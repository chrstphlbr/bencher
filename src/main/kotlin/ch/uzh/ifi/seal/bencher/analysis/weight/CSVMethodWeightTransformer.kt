package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.*
import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.NoMethod
import ch.uzh.ifi.seal.bencher.analysis.AccessModifier
import ch.uzh.ifi.seal.bencher.analysis.WalaProperties
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGInclusions
import ch.uzh.ifi.seal.bencher.analysis.callgraph.Coverages
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.AllApplicationEntrypoints
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaSCG
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaSCGAlgo
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.bencherMethod
import ch.uzh.ifi.seal.bencher.analysis.finder.IncompleteMethodFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.IterableMethodFinder
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.util.config.AnalysisScopeReader
import java.io.OutputStream
import java.nio.file.Path

class CSVMethodWeightTransformer(
        private val jar: Path,
        private val methodWeighter: MethodWeighter,
        private val methodWeightMapper: MethodWeightMapper,
        private val output: OutputStream,
        private val walaSCGAlgo: WalaSCGAlgo,
        private val cgInclusions: CGInclusions,
        private val reflectionOptions: AnalysisOptions.ReflectionOptions,
        private val packagePrefixes: Set<String>? = null
) : CommandExecutor {
    override fun execute(): Option<String> {
        val emws = methodWeighter.weights(methodWeightMapper)
        val mws = emws.getOrHandle {
            return Some(it)
        }

        // methods from weighter
        val methods = mws.map { it.key }
        val imf = IncompleteMethodFinder(
                methods = methods,
                jar = jar,
                acceptedAccessModifier = setOf(AccessModifier.PUBLIC)
        )
        val eFqnMethods = imf.bencherWalaMethods()
        // (potentially) fully-qualified methods
        val fqnMethods = eFqnMethods.getOrHandle {
            return Some(it)
        }

        val fqnMethodPairs = methods.mapIndexedNotNull { i, m ->
            val fqnm = fqnMethods[i]
            if (fqnm.first == NoMethod) {
                null
            } else {
                Pair(m, fqnm)
            }
        }

        // transform interfaces to concrete classes
        val eims = implementingMethods(fqnMethodPairs)
        // list of pairs of non-fully-qualified methods (as from methods/MethodWeighter) and
        // a corresponding fully-qualified concrete method (or NoMethod)
        val concreteMethodPairs = eims.getOrHandle {
            return Some(it)
        }

        // get callgraphs
        val ecgs = callgraphs(concreteMethodPairs.map { it.second })
        val cgs = ecgs.getOrHandle {
            return Some(it)
        }

        // calculate weights based on CG
        val newWeights = newMethodWeights(mws, concreteMethodPairs, cgs)

        val p = CSVMethodWeightPrinter(output)
        p.print(newWeights)

        return None
    }

    private fun implementingMethods(methods: List<Pair<Method, Pair<Method, IMethod?>>>): Either<String, List<Pair<Method, Method>>> {
        val scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jar.toAbsolutePath().toString(), WalaProperties.exclFile.fileResource())
                ?: return Either.Left("Could not create Wala scope")
        val ch = ClassHierarchyFactory.make(scope) ?: return Either.Left("Could not create class hierarchy")

        return Either.Right(
                methods.flatMap { (o, n) ->
                    if (n.first == NoMethod) {
                        listOf(Pair(o, NoMethod))
                    } else {
                        val im = n.second
                        if (im == null) {
                            // should not happen as this would violate that n.first != NoMethod
                            return Either.Left("No IMethod for method ${n.first}")
                        } else {
                            ch.getPossibleTargets(im.reference).map {
                                Pair(o, it.bencherMethod())
                            }
                        }
                    }
                }
        )
    }

    private fun callgraphs(methods: Iterable<Method>): Either<String, Coverages> {
        val cgExecutor = WalaSCG(
                entrypoints = AllApplicationEntrypoints(
                        mf = IterableMethodFinder(methods),
                        packagePrefixes = packagePrefixes
                ),
                algo = walaSCGAlgo,
                inclusions = cgInclusions,
                reflectionOptions = reflectionOptions
        )
        return cgExecutor.get(jar)
    }

    private fun newMethodWeights(oldWeights: MethodWeights, concreteMethodPairs: List<Pair<Method, Method>>, coverages: Coverages): MethodWeights {
        // assign method weights to concrete methods
        val omws = concreteMethodPairs.associate {
            Pair(it.second, oldWeights[it.first] ?: 0.0)
        }

        val nmws = mutableMapOf<Method, Double>()
        // add oldWeights to new weights
        nmws.putAll(omws)

        // iterate over the API methods
        coverages.coverages.forEach { api, calls ->
            val apiWeight = omws[api] ?: 0.0
            val seen = mutableSetOf<Method>()

            // assign API weight to each (potentially) reachable method
            calls.all(true).forEach rm@{
                val m = it.unit.toPlainMethod()

                // only assign API weight once to a reachable method
                if (seen.contains(m)) {
                    return@rm
                }
                seen.add(m)

                val callWeight = nmws[m]
                nmws[m] = if (callWeight == null) {
                    apiWeight
                } else {
                    apiWeight + callWeight
                }
            }
        }

        return nmws
    }
}
