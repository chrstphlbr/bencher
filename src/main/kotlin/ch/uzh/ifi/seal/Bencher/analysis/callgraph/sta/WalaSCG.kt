package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.MethodCall
import ch.uzh.ifi.seal.bencher.analysis.callgraph.merge
import com.ibm.wala.ipa.callgraph.*
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.util.config.AnalysisScopeReader
import org.funktionale.either.Either
import java.util.*


class WalaSCG(
        private val jar: String,
        private val entrypoints: EntrypointsGenerator,
        private val algo: WalaSCGAlgo,
        private val inclusions: WalaSCGInclusions = IncludeAll
) : CGExecutor {

    override fun get(): Either<String, CGResult> {
        val ef = exclFile.fileResource()
        if (!ef.exists()) {
            return Either.left("Exclusions file '$exclFile' does not exist")
        }

        val scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jar, ef)
        val ch = ClassHierarchyFactory.make(scope)

        val eeps = entrypoints.generate(ch)
        if (eeps.isLeft()) {
            return Either.left("Could not generate entry points: ${eeps.left().get()}")
        }

        val multipleEps = eeps.right().get()

        val multipleCgResults = multipleEps.map { eps ->
            val usedEps = eps.map { it.second }
            val opt = AnalysisOptions(scope, usedEps)
            opt.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL)

            val cache = AnalysisCacheImpl()
            val cg = algo.cg(opt, scope, cache, ch)

            val benchEps: Iterable<Pair<Benchmark, Entrypoint>> = eps.mapNotNull { (m, ep) ->
                when (m) {
                    is PlainMethod -> null
                    is PossibleMethod -> null
                    is Benchmark -> Pair(m, ep)
                }
            }
            transformCg(cg, benchEps, scope)
        }.merge()

        return Either.right(multipleCgResults)
    }

    private fun <T : Iterable<Pair<Benchmark, Entrypoint>>> transformCg(cg: CallGraph, benchs: T, scope: AnalysisScope): CGResult {
        val benchCalls: Map<Benchmark, Iterable<MethodCall>> = benchs.mapNotNull entrypoint@{ (bench, ep) ->
            val m = ep.method ?: return@entrypoint null
            val mref = m.reference ?: return@entrypoint null
            val cgNodes = cg.getNodes(mref)
            Pair(bench, handleBFS(cg, LinkedList(cgNodes), scope))
        }.toMap()

        return CGResult(
                benchCalls = benchCalls
        )
    }

    private tailrec fun handleBFS(cg: CallGraph,
                                  cgNodes: Queue<CGNode>,
                                  scope: AnalysisScope,
                                  ret: MutableList<MethodCall> = mutableListOf(),
                                  seen: MutableSet<CGNode> = mutableSetOf(),
                                  level: Int = 1
    ): Iterable<MethodCall> {

        if (cgNodes.peek() == null) {
            return ret
        }

        val nextLevelQ = LinkedList<CGNode>()
        val seenLevel = mutableSetOf<CGNode>()

        while (cgNodes.peek() != null) {
            val n = cgNodes.poll() ?: break // should never break here because of loop condition (poll)

            if (seen.contains(n)) {
                continue
            }

            n.iterateCallSites().asSequence().forEachIndexed cs@{ i, csr ->
                if (!scope.applicationLoader.equals(csr.declaredTarget.declaringClass.classLoader)) {
                    // only care about application class loader targets
                    return@cs
                }

                val targets = cg.getPossibleTargets(n, csr)
                val nrPossibleTargets = targets.size
                targets.forEach { tn ->
                    if (!seenLevel.contains(tn) && !seen.contains(tn)) {
                        val m = possibleMethod(tn.method.bencherMethod(), Pair(nrPossibleTargets, i))
                        val tml = MethodCall(m, level)
                        add(ret, tml)
                        nextLevelQ.offer(tn)
                        seenLevel.add(tn)
                    }
                }
            }

            seen.add(n)
        }
        return handleBFS(cg, nextLevelQ, scope, ret, seen, level + 1)
    }

    fun add(l: MutableList<MethodCall>, el: MethodCall): Unit {
        val add = when (inclusions) {
            IncludeAll -> true
            is IncludeOnly -> inclusions.includes.any { el.method.clazz.startsWith(it) }
        }
        if (add) {
            l.add(el)
        }
    }

    private fun possibleMethod(m: Method, possibleTargets: Pair<Int, Int>): Method =
            if (possibleTargets.first == 1) {
                m
            } else {
                PossibleMethod(
                        clazz = m.clazz,
                        name = m.name,
                        params = m.params,
                        nrPossibleTargets = possibleTargets.first,
                        idPossibleTargets = possibleTargets.second
                )
            }

    companion object {
        val exclFile = "wala_exclusions.txt"
    }
}
