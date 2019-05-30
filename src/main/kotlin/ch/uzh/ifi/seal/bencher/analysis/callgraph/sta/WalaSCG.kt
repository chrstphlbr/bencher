package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.WalaProperties
import ch.uzh.ifi.seal.bencher.analysis.callgraph.*
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.RF
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Reachabilities
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.ReachabilityResult
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.ipa.callgraph.*
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.util.config.AnalysisScopeReader
import org.apache.logging.log4j.LogManager
import org.funktionale.either.Either
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import java.util.*


class WalaSCG(
        private val entrypoints: EntrypointsGenerator,
        private val algo: WalaSCGAlgo,
        private val reflectionOptions: AnalysisOptions.ReflectionOptions = AnalysisOptions.ReflectionOptions.FULL,
        private val inclusions: CGInclusions = IncludeAll
) : CGExecutor {

    override fun get(jar: Path): Either<String, CGResult> {
        val ef = WalaProperties.exclFile.fileResource()
        if (!ef.exists()) {
            return Either.left("Exclusions file '${WalaProperties.exclFile}' does not exist")
        }

        val scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jar.toAbsolutePath().toString(), ef)
        val ch = ClassHierarchyFactory.make(scope)

        val eeps = entrypoints.generate(scope, ch)
        if (eeps.isLeft()) {
            return Either.left("Could not generate entry points: ${eeps.left().get()}")
        }

        val multipleEps = eeps.right().get()

        val total = multipleEps.toList().size
        log.info("start generating CGs")
        val startCGS = LocalDateTime.now()
        val multipleCgResults = multipleEps.mapIndexed { i, eps ->
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
            log.info("start CG algorithm for method(s) ${methodEps.map { "${it.first.clazz}.${it.first.name}" }} (${i+1}/$total)")
            val startCG = LocalDateTime.now()
            val cg = algo.cg(opt, scope, cache, ch)
            val endCG = LocalDateTime.now()
            log.info("finished CG algorithm (${i+1}/$total) in ${Duration.between(startCG, endCG)}")

            val startTCG = LocalDateTime.now()
            val tcg = transformCg(cg, methodEps, scope)
            val endTCG = LocalDateTime.now()
            log.info("finished transforming CG (${i+1}/$total) in ${Duration.between(startTCG, endTCG)}")
            tcg
        }.merge()
        val endCGS = LocalDateTime.now()
        log.info("finished generating CGs in ${Duration.between(startCGS, endCGS)}")
        return Either.right(multipleCgResults)
    }

    private fun <T : List<Pair<Method, Entrypoint>>> transformCg(cg: CallGraph, methods: T, scope: AnalysisScope): CGResult {
        val calls = mutableMapOf<Method, Reachabilities>()
        val totalSize = methods.size
        methods.forEachIndexed entrypoint@{ i, (method, ep) ->
            val m = ep.method ?: return@entrypoint
            val mref = m.reference ?: return@entrypoint
            val cgNodes = cg.getNodes(mref)

//            val seen = mutableSetOf<CGNode>()
//            val ret = HashSet<ReachabilityResult>()
//            cgNodes.forEach { edgesDFS(scope, cg, it, seen, ret, 1, 1.0) }

            val q = LinkedList<CGNode>()
            cgNodes.forEach { q.add(it) }

            val ret = edgesBFS(scope, cg, q, mutableSetOf(), mutableSetOf(), 1)

            log.info("method ${i+1}/$totalSize $method with ${ret.size} edges")

//            val toSeen = mutableSetOf<Method>()
//            val reachabilities = ret.toSortedSet(ReachabilityResultComparator).filter {
//                val c = toSeen.contains(it.to)
//                if (!c) {
//                    toSeen.add(it.to)
//                }
//                !c
//            }.toHashSet()

            calls[method] = Reachabilities(
                    start = method,
                    reachabilities = ret
            )
        }

        return CGResult(
                calls = calls
        )
    }

    private tailrec fun edgesBFS(scope: AnalysisScope, cg: CallGraph, cgNodes: Queue<CGNode>, seen: MutableSet<CGNode>, ret: MutableSet<ReachabilityResult>, level: Int): Set<ReachabilityResult> {
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

            n.iterateCallSites().asSequence().forEach cs@{ csr ->
                if (scope.applicationLoader != csr.declaredTarget.declaringClass.classLoader) {
                    // only care about application class loader targets
                    return@cs
                }

                val targets = cg.getPossibleTargets(n, csr)
                val nrPossibleTargets = targets.size
                val newProb = 1.0/nrPossibleTargets
                targets.forEach targets@{ t ->
                    if (!seenLevel.contains(t) && !seen.contains(t)) {
                        val toBm = t.method.bencherMethod()
                        if (!inclusions.include(toBm)) {
                            return@targets
                        }

                        val r = if (newProb == 1.0) {
                            RF.reachable(
                                    from = fromBm,
                                    to = toBm,
                                    level = level
                            )
                        } else {
                            RF.possiblyReachable(
                                    from = fromBm,
                                    to = toBm,
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

    private fun edgesDFS(scope: AnalysisScope, cg: CallGraph, from: CGNode, seen: MutableSet<CGNode>, mcs: MutableSet<ReachabilityResult>, level: Int, probability: Double) {
        if (seen.contains(from)) {
            return
        }

//        val newSeen = seen + from
        seen.add(from)

        val fromBencherMethod = from.method.bencherMethod()
        from.iterateCallSites().asSequence().forEach cs@{ csr ->
            if (scope.applicationLoader != csr.declaredTarget.declaringClass.classLoader) {
                // only care about application class loader targets
                return@cs
            }

            val targets = cg.getPossibleTargets(from, csr)
            val nrPossibleTargets = targets.size
//            val newProb = newProbability(probability, nrPossibleTargets)
            val newProb = 1.0/nrPossibleTargets
            targets.forEach targets@{ t ->
                val toBencherMethod = t.method.bencherMethod()

                if (!inclusions.include(toBencherMethod)) {
                    return@cs
                }

                val n = if (newProb == 1.0) {
                    RF.reachable(
                            from = fromBencherMethod,
                            to = toBencherMethod,
                            level = level
                    )
                } else {
                    RF.possiblyReachable(
                            from = fromBencherMethod,
                            to = toBencherMethod,
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
            old * (1.0/nrCalls)

    companion object {
        val log = LogManager.getLogger(WalaSCG::class.java.canonicalName)
    }
}
