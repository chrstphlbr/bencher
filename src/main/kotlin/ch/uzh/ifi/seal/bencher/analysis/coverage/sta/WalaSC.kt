package ch.uzh.ifi.seal.bencher.analysis.coverage.sta

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.WalaProperties
import ch.uzh.ifi.seal.bencher.analysis.coverage.*
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CUF
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.Coverage
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitMethod
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitResult
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.core.util.config.AnalysisScopeReader
import com.ibm.wala.ipa.callgraph.*
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import org.apache.logging.log4j.LogManager
import java.nio.file.Path
import java.util.*


class WalaSC(
        private val entrypoints: EntrypointsGenerator,
        private val algo: WalaSCGAlgo,
        private val reflectionOptions: AnalysisOptions.ReflectionOptions = AnalysisOptions.ReflectionOptions.FULL,
        private val inclusions: CoverageInclusions = IncludeAll
) : CoverageExecutor {

    override fun get(jar: Path): Either<String, Coverages> {
        val ef = WalaProperties.exclFile.fileResource()
        if (!ef.exists()) {
            return Either.Left("Exclusions file '${WalaProperties.exclFile}' does not exist")
        }

        val scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(jar.toAbsolutePath().toString(), ef)
        val ch = ClassHierarchyFactory.make(scope)

        val multipleEps = entrypoints.generate(scope, ch).getOrElse {
            return Either.Left("Could not generate entry points: $it")
        }

        val total = multipleEps.toList().size
        log.info("start generating CGs")
        val startCGS = System.nanoTime()
        val multipleCgResults = multipleEps.mapIndexedNotNull { i, eps ->
            val usedEps = eps.map { it.second }

            val opt = AnalysisOptions(scope, usedEps)
            opt.reflectionOptions = reflectionOptions

            val methodEps: List<Pair<Method, Entrypoint>> = eps.mapNotNull { (m, ep) ->
                when (m) {
                    is CGStartMethod -> Pair(m.method, ep)
                    is CGAdditionalMethod -> null
                }
            }

            val cache = AnalysisCacheImpl()
            log.info("start CG algorithm for method(s) ${methodEps.map { "${it.first.clazz}.${it.first.name}" }} (${i + 1}/$total)")
            val startCG = System.nanoTime()
            val cg: CallGraph = try {
                algo.cg(opt, scope, cache, ch)
            } catch (rte: RuntimeException) {
                log.error("failed CG algorithm (${i + 1}/$total): ${rte.message}")
                rte.printStackTrace(System.err)
                return@mapIndexedNotNull null
            } finally {
                val durCG = System.nanoTime() - startCG
                log.info("finished CG algorithm (${i + 1}/$total) in ${durCG}ns")
            }

            val startTCG = System.nanoTime()
            val tcg = transformCg(cg, methodEps, scope)
            val durTCG = System.nanoTime() - startTCG
            log.info("finished transforming CG (${i + 1}/$total) in ${durTCG}ns")
            tcg
        }.merge()
        val durCGS = System.nanoTime() - startCGS
        log.info("finished generating CGs in ${durCGS}ns")
        return Either.Right(multipleCgResults)
    }

    private fun <T : List<Pair<Method, Entrypoint>>> transformCg(cg: CallGraph, methods: T, scope: AnalysisScope): Coverages {
        val calls = mutableMapOf<Method, Coverage>()
        val totalSize = methods.size
        methods.forEachIndexed entrypoint@{ i, (method, ep) ->
            val m = ep.method ?: return@entrypoint
            val mref = m.reference ?: return@entrypoint
            val cgNodes = cg.getNodes(mref)

            val q = LinkedList<CGNode>()
            cgNodes.forEach { q.add(it) }

            val ret = edgesBFS(scope, cg, q, mutableSetOf(), mutableSetOf(), 1)

            log.info("method ${i + 1}/$totalSize $method with ${ret.size} edges")

            calls[method] = Coverage(
                    of = method,
                    unitResults = ret
            )
        }

        return Coverages(
                coverages = calls
        )
    }

    private tailrec fun edgesBFS(scope: AnalysisScope, cg: CallGraph, cgNodes: Queue<CGNode>, seen: MutableSet<CGNode>, ret: MutableSet<CoverageUnitResult>, level: Int): Set<CoverageUnitResult> {
        if (cgNodes.peek() == null) {
            return ret
        }

        val nextLevelQ = LinkedList<CGNode>()

        while (cgNodes.peek() != null) {
            val n = cgNodes.poll() ?: break // should never break here because of loop condition (poll)

            if (seen.contains(n)) {
                continue
            }

            val seenLevel = mutableSetOf<CGNode>()

            val fromBm = n.method.bencherMethod()

            n.iterateCallSites().forEach cs@{ csr ->
                if (scope.applicationLoader != csr.declaredTarget.declaringClass.classLoader) {
                    // only care about application class loader targets
                    return@cs
                }

                val targets = cg.getPossibleTargets(n, csr)
                val nrPossibleTargets = targets.size
                val newProb = 1.0 / nrPossibleTargets
                targets.forEach targets@{ t ->
                    if (!seenLevel.contains(t) && !seen.contains(t)) {
                        val toBm = t.method.bencherMethod()
                        if (!inclusions.include(toBm)) {
                            return@targets
                        }

                        val r = if (newProb == 1.0) {
                            CUF.covered(
                                    of = fromBm,
                                    unit = CoverageUnitMethod(method = toBm),
                                    level = level
                            )
                        } else {
                            CUF.possiblyCovered(
                                    of = fromBm,
                                    unit = CoverageUnitMethod(method = toBm),
                                    level = level,
                                    probability = newProb
                            )
                        }

                        ret.add(r)

                        nextLevelQ.offer(t)
                        seenLevel.add(t)
                    }
                }
            }

            seen.add(n)
        }
        return edgesBFS(scope, cg, nextLevelQ, seen, ret, level + 1)
    }

    private fun edgesDFS(scope: AnalysisScope, cg: CallGraph, from: CGNode, seen: MutableSet<CGNode>, mcs: MutableSet<CoverageUnitResult>, level: Int, probability: Double) {
        if (seen.contains(from)) {
            return
        }

//        val newSeen = seen + from
        seen.add(from)

        val fromBencherMethod = from.method.bencherMethod()
        from.iterateCallSites().forEach cs@{ csr ->
            if (scope.applicationLoader != csr.declaredTarget.declaringClass.classLoader) {
                // only care about application class loader targets
                return@cs
            }

            val targets = cg.getPossibleTargets(from, csr)
            val nrPossibleTargets = targets.size
//            val newProb = newProbability(probability, nrPossibleTargets)
            val newProb = 1.0 / nrPossibleTargets
            targets.forEach targets@{ t ->
                val toBencherMethod = t.method.bencherMethod()

                if (!inclusions.include(toBencherMethod)) {
                    return@cs
                }

                val n = if (newProb == 1.0) {
                    CUF.covered(
                            of = fromBencherMethod,
                            unit = CoverageUnitMethod(method = toBencherMethod),
                            level = level
                    )
                } else {
                    CUF.possiblyCovered(
                            of = fromBencherMethod,
                            unit = CoverageUnitMethod(method = toBencherMethod),
                            level = level,
                            probability = newProb
                    )
                }

//                MCF.methodCall(
//                        from = fromBencherMethod,
//                        to = toBencherMethod,
//                        nrPossibleTargets = nrPossibleTargets,
//                        idPossibleTargets = idPossibleTargets
//                )

                mcs.add(n)

                if (!seen.contains(t)) {
                    edgesDFS(scope, cg, t, seen, mcs, level + 1, newProb)
                }
            }
        }
    }

    private fun newProbability(old: Double, nrCalls: Int): Double = independantProbability(old, nrCalls)

    private fun independantProbability(old: Double, nrCalls: Int): Double =
            old * (1.0 / nrCalls)

    companion object {
        val log = LogManager.getLogger(WalaSC::class.java.canonicalName)
    }
}
