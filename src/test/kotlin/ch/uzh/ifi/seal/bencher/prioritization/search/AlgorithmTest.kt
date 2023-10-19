package ch.uzh.ifi.seal.bencher.prioritization.search

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.weight.CoverageUnitWeights
import ch.uzh.ifi.seal.bencher.analysis.weight.CoveragesWeighter
import ch.uzh.ifi.seal.bencher.measurement.PerformanceChangesTestHelper
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizerTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.uma.jmetal.algorithm.Algorithm
import org.uma.jmetal.solution.permutationsolution.PermutationSolution
import org.uma.jmetal.util.aggregationfunction.impl.WeightedSum

abstract class AlgorithmTest {
    protected lateinit var benchs: List<Benchmark>
    protected lateinit var benchmarkIdMap: BenchmarkIdMap
    protected lateinit var cov: Coverages
    protected lateinit var weights: CoverageUnitWeights
    protected lateinit var covObjective: CoverageObjective
    protected lateinit var covOverlapObjective: CoverageOverlapObjective
    protected lateinit var changeHistoryObjective: ChangeHistoryObjective

    @BeforeEach
    fun setUp() {
        benchs = PrioritizerTestHelper.benchs
        benchmarkIdMap = BenchmarkIdMapImpl(benchs)
        cov = PrioritizerTestHelper.covFull
        weights = CoveragesWeighter(cov).weights()
            .getOrElse { Assertions.fail("could not get CoverageUnitWeights: $it") }

        // objectives
        covObjective = CoverageObjective(cov, weights)
        covOverlapObjective = CoverageOverlapObjective(cov)
        changeHistoryObjective = ChangeHistoryObjective(PerformanceChangesTestHelper.changes)
    }

    protected fun aggregation(objectives: List<Objective>, normalize: Boolean = true): Aggregation = Aggregation(
        function = WeightedSum(normalize),
        weights = objectives.indices.map { 1.0 / objectives.size }.toDoubleArray(),
        objectives = if (!normalize) { null } else { objectives },
    )

    protected fun runAlgorithmCheckResult(a: Algorithm<PermutationSolution<Int>>): List<PrioritizedMethod<Benchmark>> {
        return checkResult(runAlgorithm(a))
    }

    private fun runAlgorithm(a: Algorithm<PermutationSolution<Int>>): PermutationSolution<Int> {
        a.run()
        return a.result()
    }

    private fun checkResult(s: PermutationSolution<Int>): List<PrioritizedMethod<Benchmark>> {
        val results = JMetalPrioritizer.transformSolutions(benchmarkIdMap, listOf(s))
            .getOrElse { Assertions.fail("could not transform solutions: $it") }

        Assertions.assertEquals(1, results.size)

        val result = results[0]

        Assertions.assertEquals(4, result.size)

        return result
    }
}
