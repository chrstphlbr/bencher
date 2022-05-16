package ch.uzh.ifi.seal.bencher.prioritization.search

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.getOrHandle
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Version
import ch.uzh.ifi.seal.bencher.analysis.change.Change
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoverageOverlap
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
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.random.Random

class JMetalPrioritizer(
    private val coverage: Coverages,
    private val coverageUnitWeights: CoverageUnitWeights,
    performanceChanges: PerformanceChanges?,
    changes: Set<Change>?,
    private val project: String,
    private val v1: Version,
    private val v2: Version,
    override val random: Random = Random(System.nanoTime()),
    private val searchAlgorithm: SearchAlgorithm,
    private val objectives: Set<Objective>,
    private val fileOutputFolder: Path? = null,
    private val fileOutputPostfix: String = ""
) : PrioritizerMultipleSolutions {

    private val deltaCoverage: Coverages?
    private val overlap: CoverageOverlap?
    private val performanceChanges: PerformanceChanges?

    init {
        // set delta coverage
        this.deltaCoverage = when {
            objectives.contains(DeltaCoverageObjective) && changes == null -> throw IllegalArgumentException("parameter changes required for objective DeltaCoverage")
            objectives.contains(DeltaCoverageObjective) && changes != null -> coverage.onlyChangedCoverages(changes)
            else -> null
        }

        // set coverage overlap
        this.overlap = if (objectives.contains(CoverageOverlapObjective)) {
            CoverageOverlapImpl(coverage.coverages.map { it.value })
        } else {
            null
        }

        // set performance changes
        this.performanceChanges = if (objectives.contains(ChangeHistoryObjective)) {
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
        val cov = prepareCoverage(benchs)

        val benchmarks = cov.coverages.keys.map { m ->
            m as? Benchmark ?: return Either.Left("method not a benchmark: $m")
        }

        val bim = BenchmarkIndexMapImpl(benchmarks)

        val numberOfBenchmarks = cov.coverages.size

        val problem = PrioritizationProblem(
            benchmarkIndexMap = bim,
            objectives = objectives,
            coverage = cov,
            deltaCoverage = deltaCoverage,
            coverageUnitWeights = coverageUnitWeights,
            coverageOverlap = overlap,
            performanceChanges = performanceChanges
        )

        val options = SearchAlgorithmOptions(
            numberOfBenchmarks = numberOfBenchmarks
        )

        val algorithm: Algorithm<List<PermutationSolution<Int>>> = searchAlgorithm.create(problem, options)

        algorithm.run()

        val solutionList = algorithm.result

        saveJMetalFiles(solutionList)

        return transformJMetalSolutions(bim, solutionList)
    }

    private fun prepareCoverage(benchs: Iterable<Benchmark>): Coverages =
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

    private fun transformJMetalSolutions(indexer: BenchmarkIndexMap, solutionList: List<PermutationSolution<Int>>): Either<String, List<List<PrioritizedMethod<Benchmark>>>> {
        val benchmarkSolutions = solutionList.map { solution ->
            val bs = indexer
                .benchmarks(solution.variables())
                .getOrHandle {
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
