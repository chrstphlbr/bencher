package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.PossibleMethod
import ch.uzh.ifi.seal.bencher.analysis.byteCode
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.MethodCall
import ch.uzh.ifi.seal.bencher.analysis.callgraph.WalaCGResult
import ch.uzh.ifi.seal.bencher.analysis.finder.MethodFinder
import ch.uzh.ifi.seal.bencher.analysis.sourceCode
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.*
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.ipa.cha.IClassHierarchy
import com.ibm.wala.types.ClassLoaderReference
import com.ibm.wala.types.TypeReference
import com.ibm.wala.util.config.AnalysisScopeReader
import org.funktionale.either.Either
import java.io.File
import java.util.*


class WalaSCG(
        private val jar: String,
        private val bf: MethodFinder<Benchmark>,
        private val algo: WalaSCGAlgo
) : CGExecutor<WalaCGResult> {

    override fun get(): Either<String, WalaCGResult> {
        val benchs = bf.all()
        if (benchs.isLeft()) {
            return Either.left(benchs.left().get())
        }

        val bs = benchs.right().get()

        val ef = File(this::class.java.classLoader.getResource(exclFile).file)
        if (!ef.exists()) {
            return Either.left("Exclusions file '$exclFile' does not exist")
        }

        val scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jar, ef)
        val ch = ClassHierarchyFactory.make(scope)

        val eps = entryPoints(bs, ch)
        if (eps.size < bs.size) {
            return Either.left("Could not find entry points (size: ${eps.size}) for all benchmarks (size: ${bs.size})")
        }

        val opt = AnalysisOptions(scope, eps.map { it.second })
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
        return Either.right(transformCg(cg, benchEps, ch, scope))
    }

    private fun entryPoints(benchs: Iterable<Benchmark>, ch: IClassHierarchy): List<Pair<Method, Entrypoint>> =
            benchs.map { entryPoint(ch, it) }.flatten()

    private fun entryPoint(ch: IClassHierarchy, bench: Benchmark): List<Pair<Method, Entrypoint>> {
        val c = ch.lookupClass(TypeReference.find(ClassLoaderReference.Application, bench.clazz.byteCode))
        return c.allMethods.map {
            DefaultEntrypoint(it, ch)
        }.mapNotNull {
            val m = it.method
            if (bench.name == m.name.toString()) {
                Pair(bench, it)
            } else if (isSetupMethod(m)) {
                Pair(method(m), it)
            } else {
                null
            }
        }
    }

    private fun isSetupMethod(m: IMethod): Boolean =
            m.annotations.any { a ->
                val n = a.type.name.toUnicodeString()
                n.contains(setupAnnotation) || n.contains(tearDownAnnotation)
            }

    private fun <T : Iterable<Pair<Benchmark, Entrypoint>>> transformCg(cg: CallGraph, benchs: T, ch: ClassHierarchy, scope: AnalysisScope): WalaCGResult {
        val benchCalls: Map<Benchmark, Iterable<MethodCall>> = benchs.mapNotNull entrypoint@{ (bench, ep) ->
            val m = ep.method ?: return@entrypoint null
            val mref = m.reference ?: return@entrypoint null
            val cgNodes = cg.getNodes(mref)
            Pair(bench, handleBFS(cg, LinkedList(cgNodes), scope))
        }.toMap()

        return WalaCGResult(
                toolCg = cg,
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
                        val tml = MethodCall(method(tn.method, Pair(nrPossibleTargets, i)), level)
                        ret.add(tml)
                        nextLevelQ.offer(tn)
                        seenLevel.add(tn)
                    }
                }
            }

            seen.add(n)
        }
//        seen.addAll(seenLevel)
        return handleBFS(cg, nextLevelQ, scope, ret, seen, level + 1)
    }

    private fun method(m: IMethod, possibleTargets: Pair<Int, Int> = Pair(1, -1)): Method {
        val params = if (m.descriptor.parameters == null) {
            listOf()
        } else {
            m.descriptor.parameters.map { it.toUnicodeString().sourceCode }
        }

        val clazz = m.reference.declaringClass.name.toUnicodeString().sourceCode
        val name = m.name.toUnicodeString()

        if (possibleTargets.first == 1) {
            return PlainMethod(
                    clazz = clazz,
                    name = name,
                    params = params
            )
        } else {
            return PossibleMethod(
                    clazz = clazz,
                    name = name,
                    params = params,
                    nrPossibleTargets = possibleTargets.first,
                    idPossibleTargets = possibleTargets.second
            )
        }
    }

    companion object {
        val exclFile = "wala_exclusions.txt"
        private val setupAnnotation = "org/openjdk/jmh/annotations/Setup"
        private val tearDownAnnotation = "org/openjdk/jmh/annotations/TearDown"
    }
}
