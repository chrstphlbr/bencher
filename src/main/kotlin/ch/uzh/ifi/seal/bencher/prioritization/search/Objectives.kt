package ch.uzh.ifi.seal.bencher.prioritization.search

import arrow.core.Some
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.change.Change
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoverageOverlap
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.weight.CoverageUnitWeights
import ch.uzh.ifi.seal.bencher.measurement.Mean
import ch.uzh.ifi.seal.bencher.measurement.PerformanceChanges
import ch.uzh.ifi.seal.bencher.measurement.Statistic

enum class ObjectiveType(val minimization: Boolean) {
    COVERAGE(false),
    DELTA_COVERAGE(false),
    COVERAGE_OVERLAP(true),
    CHANGE_HISTORY(false);
}

interface Objective {
    val type: ObjectiveType

    val function: ObjectiveFunction

    val maxIndividual: Double
    val maxList: Double
        get() = function.max

    val minIndividual: Double
    val minList: Double
        get() = function.min

    fun compute(benchmark: Benchmark): Double

    fun compute(benchmarks: List<Benchmark>): Double = function.compute(benchmarks.map { compute(it) })

    fun compute(ids: List<Int>, idMapper: BenchmarkIdMap): Double = idMapper
        .benchmarks(ids)
        .map { compute(it) }
        .getOrElse { throw IllegalArgumentException(it) }

    companion object {
        fun toMinimization(type: ObjectiveType, value: Double): Double = if (!type.minimization) {
            value * -1
        } else {
            value
        }
    }
}

abstract class AbstractCoverageObjective(
    coverage: Coverages,
    coverageUnitWeights: CoverageUnitWeights,
) : Objective {

    protected val cov: Map<Benchmark, Double>
    final override val maxIndividual: Double
    final override val minIndividual: Double

    init {
        cov = transformCoverage(coverage, coverageUnitWeights)
        maxIndividual = cov.maxOf { it.value }
        minIndividual = cov.minOf { it.value }
    }

    private fun transformCoverage(cov: Coverages, coverageUnitWeights: CoverageUnitWeights): Map<Benchmark, Double> =
        cov.coverages.map { (m, c) ->
            Pair(
                m as Benchmark,
                c
                    .all(true)
                    .map { coverageUnitWeights.getOrDefault(it.unit, 1.0) }
                    .fold(0.0) { acc, d -> acc + d }
            )
        }.toMap()

    override fun compute(benchmark: Benchmark): Double =
        cov[benchmark] ?: throw IllegalArgumentException("no coverage for benchmark $benchmark")
}

class CoverageObjective(
    coverage: Coverages,
    coverageUnitWeights: CoverageUnitWeights,
    override val function: ObjectiveFunction = AveragePercentage(),
) : AbstractCoverageObjective(coverage, coverageUnitWeights) {
    override val type: ObjectiveType = ObjectiveType.COVERAGE
}

class DeltaCoverageObjective(
    coverage: Coverages,
    coverageUnitWeights: CoverageUnitWeights,
    changes: Set<Change>,
    override val function: ObjectiveFunction = AveragePercentage(),
) : AbstractCoverageObjective(coverage.onlyChangedCoverages(changes), coverageUnitWeights) {
    override val type: ObjectiveType = ObjectiveType.DELTA_COVERAGE
}

class CoverageOverlapObjective(
    private val coverageOverlap: CoverageOverlap,
    override val function: ObjectiveFunction = AveragePercentage(defaultEmptyList = 1.0, defaultListSumZero = 2.0),
) : Objective {
    override val type: ObjectiveType = ObjectiveType.COVERAGE_OVERLAP
    override val maxIndividual: Double = 1.0
    override val minIndividual: Double = 0.0

    override fun compute(benchmark: Benchmark): Double  = coverageOverlap.overlappingPercentage(benchmark)
}

class ChangeHistoryObjective(
    private val performanceChanges: PerformanceChanges,
    override val function: ObjectiveFunction = AveragePercentage(),
    private val statistic: Statistic<Int, Double> = Mean,
) : Objective {
    override val type: ObjectiveType = ObjectiveType.CHANGE_HISTORY
    override val maxIndividual: Double
    override val minIndividual: Double

    init {
        // all performance changes of all benchmarks in all versions
        val allChanges = performanceChanges.benchmarks()
            .flatMap { b ->
                performanceChanges.changes(b).getOrElse {
                    throw IllegalStateException("no changes for benchmark $b")
                }
            }

        if (allChanges.isEmpty()) {
            maxIndividual = 0.0
            minIndividual = 0.0
        } else {
            maxIndividual = allChanges.maxOf { it.min }.toDouble()
            minIndividual = allChanges.minOf { it.min }.toDouble()
        }
    }

    override fun compute(benchmark: Benchmark): Double = performanceChanges
        .benchmarkChangeStatistic(benchmark, statistic, Some(0.0))
        .getOrElse { throw IllegalArgumentException("no benchmark change statistic for $benchmark") }
}
