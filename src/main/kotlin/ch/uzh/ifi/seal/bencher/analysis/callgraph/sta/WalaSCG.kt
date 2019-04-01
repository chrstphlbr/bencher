package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.WalaProperties
import ch.uzh.ifi.seal.bencher.analysis.callgraph.*
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
        private val inclusions: WalaSCGInclusions = IncludeAll
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
        val calls = mutableMapOf<Method, CG>()

        methods.forEachIndexed entrypoint@{ i, (method, ep) ->
            val m = ep.method ?: return@entrypoint
            val mref = m.reference ?: return@entrypoint
            val cgNodes = cg.getNodes(mref)

            val seen = mutableSetOf<CGNode>()
            val ret = HashSet<MethodCall>()
            cgNodes.forEach { edgesDFS(scope, cg, it, seen, ret) }
//            val mcs = TreeSet<MethodCall>(MethodCallComparator)
//            val ret = edgesBFS(scope, cg, LinkedList(cgNodes), mutableSetOf(), mcs)
            log.info("method #$i $method with ${ret.size} edges")

            calls[method] = CG(
                    start = method,
                    edges = ret.toSortedSet(MethodCallComparator)
            )
        }

        return CGResult(
                calls = calls
        )
    }

    private tailrec fun edgesBFS(scope: AnalysisScope, cg: CallGraph, cgNodes: Queue<CGNode>, seen: MutableSet<CGNode>, ret: MutableSet<MethodCall>): Set<MethodCall> {
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

            n.iterateCallSites().asSequence().forEachIndexed cs@{ idPossibleTargets, csr ->
                if (scope.applicationLoader != csr.declaredTarget.declaringClass.classLoader) {
                    // only care about application class loader targets
                    return@cs
                }

                val targets = cg.getPossibleTargets(n, csr)
                val nrPossibleTargets = targets.size
                targets.forEach targets@{ t ->
                    if (!seenLevel.contains(t) && !seen.contains(t)) {
                        val toBm = t.method.bencherMethod()
                        if (!inclusions.include(toBm)) {
                            return@targets
                        }

                        val mc = MCF.methodCall(
                                from = fromBm,
                                to = toBm,
                                idPossibleTargets = idPossibleTargets,
                                nrPossibleTargets = nrPossibleTargets
                        )

                        ret.add(mc)

                        nextLevelQ.offer(t)
                        seenLevel.add(t)
                    }
                }
            }

            seen.add(n)
        }
        return edgesBFS(scope, cg, nextLevelQ, seen, ret)
    }

    private fun edgesDFS(scope: AnalysisScope, cg: CallGraph, from: CGNode, seen: MutableSet<CGNode>, mcs: MutableSet<MethodCall>) {
        if (seen.contains(from)) {
            return
        }

        seen.add(from)

        val fromBencherMethod = from.method.bencherMethod()
        from.iterateCallSites().asSequence().forEachIndexed cs@{ idPossibleTargets, csr ->
            if (scope.applicationLoader != csr.declaredTarget.declaringClass.classLoader) {
                // only care about application class loader targets
                return@cs
            }

            val targets = cg.getPossibleTargets(from, csr)
            val nrPossibleTargets = targets.size
            targets.forEach targets@{ t ->
                val toBencherMethod = t.method.bencherMethod()

                if (!inclusions.include(toBencherMethod)) {
                    return@cs
                }

                val nc = MCF.methodCall(
                        from = fromBencherMethod,
                        to = toBencherMethod,
                        nrPossibleTargets = nrPossibleTargets,
                        idPossibleTargets = idPossibleTargets
                )

                mcs.add(nc)

                if (!seen.contains(t)) {
                    edgesDFS(scope, cg, t, seen, mcs)
                }
            }
        }
    }

    companion object {
        val log = LogManager.getLogger(WalaSCG::class.java.canonicalName)
    }
}
