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
        val multipleCgResults = multipleEps.mapIndexed { i, eps ->
            val usedEps = eps.map { it.second }
            val opt = AnalysisOptions(scope, usedEps)
            opt.reflectionOptions = reflectionOptions

            val methodEps: Iterable<Pair<Method, Entrypoint>> = eps.mapNotNull { (m, ep) ->
                when (m) {
                    is CGStartMethod -> Pair(m.method, ep)
                    is CGAdditionalMethod -> null
                }
            }

            val cache = AnalysisCacheImpl()
            log.info("start CG algorithm for method(s) ${methodEps.map { "${it.first.clazz}.${it.first.name}" }} (${i+1}/$total)")
            val cg = algo.cg(opt, scope, cache, ch)

            transformCg(cg, methodEps, scope)
        }.merge()

        return Either.right(multipleCgResults)
    }

    private fun <T : Iterable<Pair<Method, Entrypoint>>> transformCg(cg: CallGraph, methods: T, scope: AnalysisScope): CGResult {
        val calls: Map<Method, CG> = methods.mapNotNull entrypoint@{ (method, ep) ->
            val m = ep.method ?: return@entrypoint null
            val mref = m.reference ?: return@entrypoint null
            val cgNodes = cg.getNodes(mref)

            val seen = mutableSetOf<Method>()
            val mcs = mutableSetOf<MethodCall>()
            cgNodes.forEach { edges(scope, cg, it, seen, mcs) }

            Pair(method, CG(
                    start = method,
                    edges = mcs.toSortedSet(MethodCallComparator)
                )
            )
        }.toMap()

        return CGResult(
                calls = calls
        )
    }

    private fun edges(scope: AnalysisScope, cg: CallGraph, from: CGNode, seen: MutableSet<Method>, mcs: MutableSet<MethodCall>) {
        val fromBencherMethod = from.method.bencherMethod()
        if (seen.contains(fromBencherMethod)) {
            return
        }

        seen += fromBencherMethod

        var i = 0
        from.iterateCallSites().forEach cs@{ csr ->
            if (!scope.applicationLoader.equals(csr.declaredTarget.declaringClass.classLoader)) {
                // only care about application class loader targets
                return@cs
            }

            val targets = cg.getPossibleTargets(from, csr)
            val nrPossibleTargets = targets.size
            targets.forEach targets@{ tn ->
                val tnbm = tn.method.bencherMethod()

                if (!include(tnbm)) {
                    return@targets
                }

                val nc = MethodCall(
                        from = fromBencherMethod,
                        to = tnbm,
                        nrPossibleTargets = nrPossibleTargets,
                        idPossibleTargets = i
                )

                mcs += nc

                if (!seen.contains(tnbm)) {
                    edges(scope, cg, tn, seen, mcs)
                }
            }
            i++
        }
    }

    private fun include(m: Method): Boolean =
            when (inclusions) {
                is IncludeAll -> true
                is IncludeOnly -> inclusions.includes.any { m.clazz.startsWith(it) }
            }

    companion object {
        val log = LogManager.getLogger(WalaSCG::class.java.canonicalName)
    }
}
