package ch.uzh.ifi.seal.bencher.prioritization.search

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Version
import ch.uzh.ifi.seal.bencher.analysis.change.Change
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoverageOverlapImpl
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.weight.CoverageUnitWeights
import ch.uzh.ifi.seal.bencher.measurement.PerformanceChange
import ch.uzh.ifi.seal.bencher.measurement.PerformanceChangeType
import ch.uzh.ifi.seal.bencher.measurement.PerformanceChanges
import ch.uzh.ifi.seal.bencher.measurement.PerformanceChangesImpl
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizerMultipleSolutions
import ch.uzh.ifi.seal.bencher.prioritization.Priority
import ch.uzh.ifi.seal.bencher.prioritization.PriorityMultiple
import org.uma.jmetal.algorithm.Algorithm
import org.uma.jmetal.solution.permutationsolution.PermutationSolution
import org.uma.jmetal.util.fileoutput.SolutionListOutput
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.random.Random

class JMetalPrioritizer(
    private val coverage: Coverages,
    private val coverageUnitWeights: CoverageUnitWeights,
    performanceChanges: PerformanceChanges?,
    private val changes: Set<Change>?,
    private val project: String,
    private val v1: Version,
    private val v2: Version,
    override val random: Random = Random(System.nanoTime()),
    private val searchAlgorithmCreator: SearchAlgorithmCreator,
    private val objectives: SortedSet<ObjectiveType>,
    private val aggregation: Aggregation? = null,
    private val fileOutputFolder: Path? = null,
    private val fileOutputPostfix: String = "",
) : PrioritizerMultipleSolutions {

    private val performanceChanges: PerformanceChanges?

    init {
        // check precondition for delta coverage
        if (objectives.contains(ObjectiveType.DELTA_COVERAGE) && changes == null) {
            throw IllegalArgumentException("parameter changes required for objective DeltaCoverage")
        }

        // set performance changes
        this.performanceChanges = if (objectives.contains(ObjectiveType.CHANGE_HISTORY)) {
            val filteredPerformanceChanges = (performanceChanges ?: noPerformanceChanges())
                .changesUntilVersion(v = v1, untilVersion1 = true, including = true)
                .getOrElse {
                    throw IllegalArgumentException("could not filter performance changes until version")
                }

            PerformanceChangesImpl(filteredPerformanceChanges)
        } else {
            null
        }

        if (fileOutputFolder != null) {
            if (!fileOutputFolder.exists()) {
                throw IllegalArgumentException("fileOutputFolder '$fileOutputFolder' does not exist")
            }
            if (!fileOutputFolder.isDirectory()) {
                throw IllegalArgumentException("fileOutputFolder '$fileOutputFolder' is not a directory")
            }
        }
    }

    override fun prioritizeMultipleSolutions(benchs: Iterable<Benchmark>): Either<String, List<List<PrioritizedMethod<Benchmark>>>> {
        val cov = prepareCoverage(benchs, coverage)

        val objectives = objectives(objectives, cov)

        val benchmarks = cov.coverages.keys.map { m ->
            m as? Benchmark ?: return Either.Left("method not a benchmark: $m")
        }

        val bim = BenchmarkIdMapImpl(benchmarks)

        val problem = PrioritizationProblem(
            benchmarkIdMap = bim,
            objectives = objectives,
            aggregate = aggregation,
        )

        val options = SearchAlgorithmOptions(
            benchmarkIdMap = bim,
            objectives = objectives,
        )

        val algorithm: Algorithm<List<PermutationSolution<Int>>> = searchAlgorithmCreator.create(problem, options)

        algorithm.run()

        val solutionList = algorithm.result()

        saveJMetalFiles(solutionList)

        return transformSolutions(bim, solutionList)
    }

    private fun objectives(objectiveTypes: SortedSet<ObjectiveType>, cov: Coverages): List<Objective> =
        objectiveTypes.map { t ->
            when (t) {
                ObjectiveType.COVERAGE -> CoverageObjective(
                    coverage = cov,
                    coverageUnitWeights = coverageUnitWeights,
                )
                ObjectiveType.DELTA_COVERAGE -> DeltaCoverageObjective(
                    coverage = cov,
                    coverageUnitWeights = coverageUnitWeights,
                    changes = changes!!, // ensured that changes is not null in constructor
                )
                ObjectiveType.COVERAGE_OVERLAP -> CoverageOverlapObjective(
                    coverageOverlap = CoverageOverlapImpl(cov.coverages.map { it.value }), // maybe could be simplified to just cov
                )
                ObjectiveType.CHANGE_HISTORY -> ChangeHistoryObjective(
                    performanceChanges = performanceChanges!!, // ensured that changes is not null in constructor
                )
            }
        }

    private fun prepareCoverage(benchs: Iterable<Benchmark>, coverage: Coverages): Coverages =
        Coverages(
            coverages = benchs
                .asSequence()
                .filter{ coverage.coverages[it] != null }
                .associateWith { coverage.coverages[it]!! }
        )

    private fun noPerformanceChanges(): PerformanceChanges =
        PerformanceChangesImpl(
            changes = coverage.coverages
                .asSequence()
                // assume that there are only Benchmark objects in there, otherwise a runtime exception is acceptable
                .map { (m, _) -> m as Benchmark }
                .map { noPerformanceChange(it)}
                .toList()
        )

    private fun noPerformanceChange(b: Benchmark): PerformanceChange =
        PerformanceChange(
            benchmark = b,
            v1 = v1,
            v2 = v2,
            type = PerformanceChangeType.NO,
            min = 0,
            max = 0,
        )

    private fun saveJMetalFiles(solutionList: List<PermutationSolution<Int>>) {
        if (fileOutputFolder == null) {
            return
        }

        val pf = if (fileOutputPostfix.isNotBlank()) {
                "-$fileOutputPostfix"
            } else {
                fileOutputPostfix
            }

        val prefix = "$project-${Version.to(v1)}-${Version.to(v2)}"
        val funFile = fileOutputFolder.resolve("$prefix-FUN$pf.csv")
        val varFile = fileOutputFolder.resolve("$prefix-VAR$pf.csv")
        SolutionListOutput(solutionList)
            .setFunFileOutputContext(DefaultFileOutputContext(funFile.toString()))
            .setVarFileOutputContext(DefaultFileOutputContext(varFile.toString()))
            .print()
    }

    companion object {
        fun transformSolutions(
            idMap: BenchmarkIdMap,
            solutionList: List<PermutationSolution<Int>>
        ): Either<String, List<List<PrioritizedMethod<Benchmark>>>> {
            val benchmarkSolutions = solutionList.map { solution ->
                val bs = idMap
                    .benchmarks(solution.variables())
                    .getOrElse {
                        return Either.Left("could not transform JMetal solution to benchmark solution: $it")
                    }

                val total = bs.size

                bs.mapIndexed { i, b ->
                    PrioritizedMethod(
                        method = b,
                        priority = Priority(
                            rank = i + 1,
                            total = total,
                            value = PriorityMultiple(
                                values = solution.objectives().toList()
                            )
                        )
                    )
                }
            }

            return Either.Right(benchmarkSolutions)
        }
    }
}
