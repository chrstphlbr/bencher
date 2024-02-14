package ch.uzh.ifi.seal.bencher.prioritization

import arrow.core.*
import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.JMHVersionExtractor
import ch.uzh.ifi.seal.bencher.analysis.change.Change
import ch.uzh.ifi.seal.bencher.analysis.change.JarChangeFinder
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchFinder
import ch.uzh.ifi.seal.bencher.analysis.weight.CSVMethodWeighter
import ch.uzh.ifi.seal.bencher.analysis.weight.CoverageUnitWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.CoveragesWeighter
import ch.uzh.ifi.seal.bencher.analysis.weight.IdentityMethodWeightMapper
import ch.uzh.ifi.seal.bencher.execution.*
import ch.uzh.ifi.seal.bencher.measurement.PerformanceChanges
import ch.uzh.ifi.seal.bencher.prioritization.greedy.AdditionalPrioritizer
import ch.uzh.ifi.seal.bencher.prioritization.greedy.TotalPrioritizer
import ch.uzh.ifi.seal.bencher.prioritization.search.IBEACreator
import ch.uzh.ifi.seal.bencher.prioritization.search.JMetalPrioritizer
import ch.uzh.ifi.seal.bencher.prioritization.search.ObjectiveType
import ch.uzh.ifi.seal.bencher.prioritization.search.SearchAlgorithmOptions
import ch.uzh.ifi.seal.bencher.selection.FullChangeSelector
import ch.uzh.ifi.seal.bencher.selection.GreedyTemporalSelector
import ch.uzh.ifi.seal.bencher.selection.Selector
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import java.time.Duration

enum class PrioritizationType {
    DEFAULT,
    RANDOM,
    TOTAL,
    ADDITIONAL,
    MO_COVERAGE_OVERLAP_PERFCHANGES
}

class PrioritizationCommand(
    private val out: OutputStream,
    private val project: String,
    private val version: String,
    private val previousVersion: String,
    private val pkgPrefixes: Set<String>,
    private val v1Jar: Path,
    private val v2Jar: Path,
    private val javaSettings: JavaSettings,
    private val cov: Coverages,
    private val weights: InputStream? = null,
    private val coverageUnitWeightMapper: CoverageUnitWeightMapper = IdentityMethodWeightMapper,
    private val performanceChanges: PerformanceChanges? = null,
    private val type: PrioritizationType,
    private val paramBenchs: Boolean = true,
    private val paramBenchsReversed: Boolean = true,
    private val changeAwarePrioritization: Boolean = false,
    private val changeAwareSelection: Boolean = false,
    private val timeBudget: Duration = Duration.ZERO,
    private val jmhParams: ExecutionConfiguration = unsetExecConfig,
) : CommandExecutor {

    private val asmBenchFinder = AsmBenchFinder(jar = v2Jar.toFile(), pkgPrefixes = pkgPrefixes)
    private val jarBenchFinder = JarBenchFinder(jar = v2Jar, javaSettings = javaSettings)

    private val v1: Version = Version.from(previousVersion).getOrElse {
        throw IllegalArgumentException("could not transform '$previousVersion' into a Version object: $it")
    }
    private val v2: Version = Version.from(version).getOrElse {
        throw IllegalArgumentException("could not transform '$version' into a Version object: $it")
    }

    private val objectives = sortedSetOf(ObjectiveType.COVERAGE, ObjectiveType.COVERAGE_OVERLAP, ObjectiveType.CHANGE_HISTORY)

    override fun execute(): Option<String> {
        val asmBs = asmBenchFinder.all()
            .getOrElse {
                return Some(it)
            }

        // changes
        val changes: Set<Change>? =
            if (changeAwarePrioritization || changeAwareSelection || objectives.contains(ObjectiveType.DELTA_COVERAGE)) {
                val cf = JarChangeFinder(pkgPrefixes = pkgPrefixes)
                cf.changes(v1Jar.toFile(), v2Jar.toFile()).getOrElse {
                    return Some(it)
                }
            } else {
                null
            }

        val cov: Coverages =
            // add groups to Coverages
            addGroupsToCoverages(asmBs, this.cov)
                // remove unchanged coverage
                .let { cov ->
                    if (changeAwarePrioritization) {
                        removeUnchangedMethodsFromCoverages(cov, changes!!)
                    } else {
                        cov
                    }
                }

        // get benchmarks to prioritize from
        val bs = jarBenchFinder.all().getOrElse {
            return Some(it)
        }

        // make every parameterized benchmark a unique benchmark in the list
        val benchs: List<Benchmark> = if (paramBenchs) {
            bs.parameterizedBenchmarks(paramBenchsReversed)
        } else {
            bs
        }

        val ep: Either<String, Prioritizer> = when (type) {
            PrioritizationType.DEFAULT -> unweightedPrioritizer(DefaultPrioritizer(v2Jar, javaSettings), cov, changes)
            PrioritizationType.RANDOM -> unweightedPrioritizer(RandomPrioritizer(), cov, changes)
            PrioritizationType.TOTAL -> weightedPrioritizer(type, cov, weights, coverageUnitWeightMapper, changes)
            PrioritizationType.ADDITIONAL -> weightedPrioritizer(type, cov, weights, coverageUnitWeightMapper, changes)
            PrioritizationType.MO_COVERAGE_OVERLAP_PERFCHANGES -> weightedPrioritizer(
                type,
                cov,
                weights,
                coverageUnitWeightMapper,
                changes
            )
        }

        val prioritizer = ep.getOrElse {
            return Some(it)
        }

        return when (prioritizer) {
            is PrioritizerMultipleSolutions -> multipleSolutions(prioritizer, benchs)
            else -> singleSolution(prioritizer, benchs)
        }
    }

    private fun addGroupsToCoverages(bs: List<Benchmark>, cov: Coverages): Coverages {
        val benchToGroup = bs.associate { b ->
            Pair(benchClassMethod(b), b.group)
        }

        return Coverages(
            cov.coverages.mapKeys { (m, _) ->
                val n = benchClassMethod(m)
                val g = benchToGroup[n] ?: return@mapKeys m
                val b = m as Benchmark
                MF.benchmark(
                    clazz = b.clazz,
                    name = b.name,
                    returnType = b.returnType,
                    params = b.params,
                    jmhParams = b.jmhParams,
                    group = g,
                )
            }
        )
    }

    private fun removeUnchangedMethodsFromCoverages(cov: Coverages, maybeChanges: Set<Change>?): Coverages {
        if (!changeAwarePrioritization) {
            return cov
        }

        return cov.onlyChangedCoverages(maybeChanges!!)
    }

    private fun benchClassMethod(b: Method): String = "${b.clazz}.${b.name}"

    private fun temporalSelector(): Either<String, Selector> {
        // configurations
        val ve = JMHVersionExtractor(jar = v2Jar.toFile())
        // used JMH version
        val v = ve.getVersion().getOrElse {
            return Either.Left(it)
        }

        // deafult execution configuration
        val dec = defaultExecConfig(v)

        val bei = asmBenchFinder.benchmarkExecutionInfos().getOrElse {
            return Either.Left(it)
        }

        val cei = asmBenchFinder.classExecutionInfos().getOrElse {
            return Either.Left(it)
        }

        val configurator = OverridingConfigBasedConfigurator(
            overridingExecConfig = jmhParams,
            benchExecConfigs = bei,
            classExecConfigs = cei,
            defaultExecConfig = dec,
        )

        return Either.Right(
            GreedyTemporalSelector(
                budget = timeBudget,
                timePredictor = ConfigExecTimePredictor(configurator = configurator),
            )
        )
    }

    private fun unweightedPrioritizer(
        p: Prioritizer,
        cov: Coverages,
        changes: Set<Change>?,
    ): Either<String, Prioritizer> = changeAwareSelectionPrioritizer(p, cov, changes)

    private fun weightedPrioritizer(
        type: PrioritizationType,
        cov: Coverages,
        weights: InputStream?,
        coverageUnitWeightMapper: CoverageUnitWeightMapper,
        changes: Set<Change>?,
    ): Either<String, Prioritizer> {
        val weighter = if (weights != null) {
            CSVMethodWeighter(file = weights, hasHeader = true)
        } else {
            CoveragesWeighter(cov = cov)
        }

        val ws = weighter.weights(coverageUnitWeightMapper).getOrElse {
            return Either.Left(it)
        }

        val prioritizer: Prioritizer = when (type) {
            PrioritizationType.TOTAL -> TotalPrioritizer(coverages = cov, coverageUnitWeights = ws)
            PrioritizationType.ADDITIONAL -> AdditionalPrioritizer(coverages = cov, coverageUnitWeights = ws)
            PrioritizationType.MO_COVERAGE_OVERLAP_PERFCHANGES -> JMetalPrioritizer(
                coverage = cov,
                coverageUnitWeights = ws,
                changes = changes,
                performanceChanges = performanceChanges,
                project = project,
                v1 = v1,
                v2 = v2,
                searchAlgorithmCreator = IBEACreator,
                searchAlgorithmOptions = SearchAlgorithmOptions(),
                objectives = objectives,
            )

            else -> return Either.Left("Invalid prioritizer '$type': not prioritizable")
        }

        return changeAwareSelectionPrioritizer(prioritizer, cov, changes)
    }

    private fun changeAwareSelectionPrioritizer(
        prioritizer: Prioritizer,
        coverages: Coverages,
        changes: Set<Change>?,
    ): Either<String, Prioritizer> {
        if (!changeAwareSelection) {
            return Either.Right(prioritizer)
        }

        val sap = SelectionAwarePrioritizer(
            prioritizer = prioritizer,
            selector = FullChangeSelector(
                coverages = coverages,
                changes = changes!!,
            ),
        )
        return Either.Right(sap)
    }

    private fun singleSolution(prioritizer: Prioritizer, benchs: List<Benchmark>): Option<String> {
        val prioritizedBenchs = prioritizer.prioritize(benchs).getOrElse {
            return Some(it)
        }

        // check whether benchmarks have a certain time budget for execution
        val benchsInBudget: List<PrioritizedMethod<Benchmark>> = if (timeBudget != Duration.ZERO) {
            val sel = temporalSelector().getOrElse {
                return Some(it)
            }

            val selectedBenchmarks = sel
                        .select(prioritizedBenchs.map { it.method })
                .getOrElse {
                    return Some(it)
                }


            // potentially O(nˆ2)
            prioritizedBenchs.filter { selectedBenchmarks.contains(it.method) }
        } else {
            prioritizedBenchs
        }

        val p = CSVPrioPrinter(out)
        p.print(benchsInBudget)

        return None
    }

    private fun multipleSolutions(prioritizer: PrioritizerMultipleSolutions, benchs: List<Benchmark>): Option<String> {
        val solutions = prioritizer.prioritizeMultipleSolutions(benchs).getOrElse {
            return Some(it)
        }

        val p = CSVPrioPrinter(out)
        p.printMulti(solutions)

        return None
    }
}
