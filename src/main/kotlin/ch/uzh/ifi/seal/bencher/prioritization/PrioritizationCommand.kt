package ch.uzh.ifi.seal.bencher.prioritization

import arrow.core.*
import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.JMHVersionExtractor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.change.Change
import ch.uzh.ifi.seal.bencher.analysis.change.JarChangeFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchFinder
import ch.uzh.ifi.seal.bencher.analysis.weight.CGMethodWeighter
import ch.uzh.ifi.seal.bencher.analysis.weight.CSVMethodWeighter
import ch.uzh.ifi.seal.bencher.analysis.weight.IdentityMethodWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightMapper
import ch.uzh.ifi.seal.bencher.execution.*
import ch.uzh.ifi.seal.bencher.measurement.PerformanceChanges
import ch.uzh.ifi.seal.bencher.prioritization.greedy.AdditionalPrioritizer
import ch.uzh.ifi.seal.bencher.prioritization.greedy.TotalPrioritizer
import ch.uzh.ifi.seal.bencher.prioritization.search.*
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
    private val cg: CGResult,
    private val weights: InputStream? = null,
    private val methodWeightMapper: MethodWeightMapper = IdentityMethodWeightMapper,
    private val performanceChanges: PerformanceChanges? = null,
    private val type: PrioritizationType,
    private val paramBenchs: Boolean = true,
    private val paramBenchsReversed: Boolean = true,
    private val changeAwarePrioritization: Boolean = false,
    private val changeAwareSelection: Boolean = false,
    private val timeBudget: Duration = Duration.ZERO,
    private val jmhParams: ExecutionConfiguration = unsetExecConfig
) : CommandExecutor {

    private val asmBenchFinder = AsmBenchFinder(jar = v2Jar.toFile(), pkgPrefixes = pkgPrefixes)
    private val jarBenchFinder = JarBenchFinder(jar = v2Jar)

    private val v1: Version = Version.from(previousVersion).getOrHandle {
        throw IllegalArgumentException("could not transform '$previousVersion' into a Version object: $it")
    }
    private val v2: Version = Version.from(version).getOrHandle {
        throw IllegalArgumentException("could not transform '$version' into a Version object: $it")
    }

    private val objectives = setOf(Coverage, CoverageOverlap, ChangeHistory)

    override fun execute(): Option<String> {
        val asmBs = asmBenchFinder.all()
            .getOrHandle {
                return Some(it)
            }

        // changes
        val changes: Set<Change>? = if (changeAwarePrioritization || changeAwareSelection || objectives.contains(DeltaCoverage)) {
            val cf = JarChangeFinder(pkgPrefixes = pkgPrefixes)
            cf.changes(v1Jar.toFile(), v2Jar.toFile()).getOrHandle {
                return Some(it)
            }
        } else {
            null
        }

        val cg: CGResult =
            // add groups to CGResults
            addGroupsToCGResult(asmBs, this.cg)
            // remove unchanged reachabilities
            .let { cg ->
                if (changeAwarePrioritization) {
                    removeUnchangedMethodsFromCGResult(cg, changes!!)
                } else {
                    cg
                }
            }

        // get benchmarks to prioritize from
        val bs = jarBenchFinder.all().getOrHandle {
            return Some(it)
        }

        // make every parameterized benchmark a unique benchmark in the list
        val benchs: List<Benchmark> = if (paramBenchs) {
            bs.parameterizedBenchmarks(paramBenchsReversed)
        } else {
            bs
        }

        val ep: Either<String, Prioritizer> = when (type) {
            PrioritizationType.DEFAULT -> unweightedPrioritizer(DefaultPrioritizer(v2Jar), cg, changes)
            PrioritizationType.RANDOM -> unweightedPrioritizer(RandomPrioritizer(), cg, changes)
            PrioritizationType.TOTAL -> weightedPrioritizer(type, cg, weights, methodWeightMapper, changes)
            PrioritizationType.ADDITIONAL -> weightedPrioritizer(type, cg, weights, methodWeightMapper, changes)
            PrioritizationType.MO_COVERAGE_OVERLAP_PERFCHANGES -> weightedPrioritizer(type, cg, weights, methodWeightMapper, changes)
        }

        val prioritizer = ep.getOrHandle {
            return Some(it)
        }

        return when (prioritizer) {
            is PrioritizerMultipleSolutions -> multipleSolutions(prioritizer, benchs)
            else -> singleSolution(prioritizer, benchs)
        }
    }

    private fun addGroupsToCGResult(bs: List<Benchmark>, cg: CGResult): CGResult {
        val benchToGroup = bs.associate { b ->
            Pair(benchClassMethod(b), b.group)
        }

        return CGResult(
                cg.calls.mapKeys { (m, _) ->
                    val n = benchClassMethod(m)
                    val g = benchToGroup[n] ?: return@mapKeys m
                    val b = m as Benchmark
                    MF.benchmark(
                            clazz = b.clazz,
                            name = b.name,
                            returnType = b.returnType,
                            params = b.params,
                            jmhParams = b.jmhParams,
                            group = g
                    )
                }
        )
    }

    private fun removeUnchangedMethodsFromCGResult(cg: CGResult, maybeChanges: Set<Change>?): CGResult {
        if (!changeAwarePrioritization) {
            return cg
        }

        return cg.onlyChangedReachabilities(maybeChanges!!)
    }

    private fun benchClassMethod(b: Method): String = "${b.clazz}.${b.name}"

    private fun temporalSelector(): Either<String, Selector> {
        // configurations
        val ve = JMHVersionExtractor(jar = v2Jar.toFile())
        // used JMH version
        val v = ve.getVersion().getOrHandle {
            return Either.Left(it)
        }

        // deafult execution configuration
        val dec = defaultExecConfig(v)

        val bei = asmBenchFinder.benchmarkExecutionInfos().getOrHandle {
            return Either.Left(it)
        }

        val cei = asmBenchFinder.classExecutionInfos().getOrHandle {
            return Either.Left(it)
        }

        val configurator = OverridingConfigBasedConfigurator(
                overridingExecConfig = jmhParams,
                benchExecConfigs = bei,
                classExecConfigs = cei,
                defaultExecConfig = dec
        )

        return Either.Right(
                GreedyTemporalSelector(
                        budget = timeBudget,
                        timePredictor = ConfigExecTimePredictor(configurator = configurator)
                )
        )
    }

    private fun unweightedPrioritizer(p: Prioritizer, cg: CGResult, changes: Set<Change>?): Either<String, Prioritizer> =
                changeAwareSelectionPrioritizer(p, cg, changes)

    private fun weightedPrioritizer(type: PrioritizationType, cg: CGResult, weights: InputStream?, methodWeightMapper: MethodWeightMapper, changes: Set<Change>?): Either<String, Prioritizer> {
        val weighter = if (weights != null) {
            CSVMethodWeighter(file = weights, hasHeader = true)
        } else {
            CGMethodWeighter(cg = cg)
        }

        val ws = weighter.weights(methodWeightMapper).getOrHandle {
            return Either.Left(it)
        }

        val prioritizer: Prioritizer = when (type) {
            PrioritizationType.TOTAL -> TotalPrioritizer(cgResult = cg, methodWeights = ws)
            PrioritizationType.ADDITIONAL -> AdditionalPrioritizer(cgResult = cg, methodWeights = ws)
            PrioritizationType.MO_COVERAGE_OVERLAP_PERFCHANGES -> JMetalPrioritizer(
                coverage = cg,
                methodWeights = ws,
                changes = changes,
                performanceChanges = performanceChanges,
                project = project,
                v1 = v1,
                v2 = v2,
                searchAlgorithm = NSGAII(),
                objectives = objectives
            )
            else -> return Either.Left("Invalid prioritizer '$type': not prioritizable")
        }

        return changeAwareSelectionPrioritizer(prioritizer, cg, changes)
    }

    private fun changeAwareSelectionPrioritizer(prioritizer: Prioritizer, cgResult: CGResult, changes: Set<Change>?): Either<String, Prioritizer> {
        if (!changeAwareSelection) {
            return Either.Right(prioritizer)
        }

        val sap = SelectionAwarePrioritizer(
                prioritizer = prioritizer,
                selector = FullChangeSelector(
                        cgResult = cgResult,
                        changes = changes!!
                )
        )
        return Either.Right(sap)
    }

    private fun singleSolution(prioritizer: Prioritizer, benchs: List<Benchmark>): Option<String> {
        val prioritizedBenchs = prioritizer.prioritize(benchs).getOrHandle {
            return Some(it)
        }

        // check whether benchmarks have a certain time budget for execution
        val benchsInBudget: List<PrioritizedMethod<Benchmark>> = if (timeBudget != Duration.ZERO) {
            val sel = temporalSelector().getOrHandle {
                return Some(it)
            }

            val selectedBenchmarks = sel
                .select(prioritizedBenchs.map { it.method })
                .getOrHandle {
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
        val solutions = prioritizer.prioritizeMultipleSolutions(benchs).getOrHandle {
            return Some(it)
        }

        val p = CSVPrioPrinter(out)
        p.printMulti(solutions)

        return None
    }
}
