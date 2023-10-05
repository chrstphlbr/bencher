package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.*
import ch.uzh.ifi.seal.bencher.CommandExecutor
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.NoMethod
import ch.uzh.ifi.seal.bencher.analysis.AccessModifier
import ch.uzh.ifi.seal.bencher.analysis.WalaProperties
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoverageInclusions
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnit
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitMethod
import ch.uzh.ifi.seal.bencher.analysis.coverage.sta.AllApplicationEntrypoints
import ch.uzh.ifi.seal.bencher.analysis.coverage.sta.WalaSC
import ch.uzh.ifi.seal.bencher.analysis.coverage.sta.WalaSCGAlgo
import ch.uzh.ifi.seal.bencher.analysis.coverage.sta.bencherMethod
import ch.uzh.ifi.seal.bencher.analysis.finder.IncompleteMethodFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.IterableMethodFinder
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.core.util.config.AnalysisScopeReader
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import java.io.OutputStream
import java.nio.file.Path

class CSVMethodWeightTransformer(
    private val jar: Path,
    private val coverageUnitWeighter: CoverageUnitWeighter,
    private val coverageUnitWeightMapper: CoverageUnitWeightMapper,
    private val output: OutputStream,
    private val walaSCGAlgo: WalaSCGAlgo,
    private val coverageInclusions: CoverageInclusions,
    private val reflectionOptions: AnalysisOptions.ReflectionOptions,
    private val packagePrefixes: Set<String>? = null
) : CommandExecutor {
    override fun execute(): Option<String> {
        val emws = coverageUnitWeighter.weights(coverageUnitWeightMapper)
        val mws = emws.getOrElse {
            return Some(it)
        }

        // methods from weighter
        val methods = mws.keys
            .mapNotNull { cu ->
                when (cu) {
                    is CoverageUnitMethod -> cu.method
                    // do not care about other CoverageUnits
                    else -> null
                }
            }
        val imf = IncompleteMethodFinder(
            methods = methods,
            jar = jar,
            acceptedAccessModifier = setOf(AccessModifier.PUBLIC)
        )
        val eFqnMethods = imf.bencherWalaMethods()
        // (potentially) fully-qualified methods
        val fqnMethods = eFqnMethods.getOrElse {
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
        val concreteMethodPairs = eims.getOrElse {
            return Some(it)
        }

        // get coverages
        val ecovs = coverages(concreteMethodPairs.map { it.second })
        val covs = ecovs.getOrElse {
            return Some(it)
        }

        // calculate weights based on coverages
        val newWeights = newMethodWeights(mws, concreteMethodPairs, covs)

        val p = CSVMethodWeightPrinter(output)
        p.print(newWeights)

        return None
    }

    private fun implementingMethods(methods: List<Pair<Method, Pair<Method, IMethod?>>>): Either<String, List<Pair<Method, Method>>> {
        val scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(jar.toAbsolutePath().toString(), WalaProperties.exclFile.fileResource())
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

    private fun coverages(methods: Iterable<Method>): Either<String, Coverages> {
        val covExecutor = WalaSC(
                entrypoints = AllApplicationEntrypoints(
                        mf = IterableMethodFinder(methods),
                        packagePrefixes = packagePrefixes
                ),
                algo = walaSCGAlgo,
                inclusions = coverageInclusions,
                reflectionOptions = reflectionOptions
        )
        return covExecutor.get(jar)
    }

    private fun newMethodWeights(oldWeights: CoverageUnitWeights, concreteMethodPairs: List<Pair<Method, Method>>, coverages: Coverages): CoverageUnitWeights {
        // assign method weights to concrete methods
        val omws = concreteMethodPairs.associate {
            val cu = CoverageUnitMethod(it.first)
            val w = oldWeights[cu] ?: 0.0
            val ncu = CoverageUnitMethod(it.second)
            Pair(ncu, w)
        }

        val nmws = mutableMapOf<CoverageUnit, Double>()
        // add oldWeights to new weights
        nmws.putAll(omws)

        // iterate over the API methods
        coverages.coverages.forEach { (api, covs) ->
            val apiCU = CoverageUnitMethod(api)
            val apiWeight = omws[apiCU] ?: 0.0
            val seen = mutableSetOf<Method>()

            // assign API weight to each (potentially) covered unit
            covs.all(true).forEach rm@{
                val mm = it.unit as? CoverageUnitMethod ?: return@rm
                val m = mm.method.toPlainMethod()
                val cu = CoverageUnitMethod(m)

                // only assign API weight once to a covered method
                if (seen.contains(m)) {
                    return@rm
                }
                seen.add(m)

                val unitWeight = nmws[cu]
                nmws[cu] = if (unitWeight == null) {
                    apiWeight
                } else {
                    apiWeight + unitWeight
                }
            }
        }

        return nmws
    }
}
