package ch.uzh.ifi.seal.bencher.prioritization.search

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.weight.CoverageUnitWeights
import ch.uzh.ifi.seal.bencher.analysis.weight.CoveragesWeighter
import ch.uzh.ifi.seal.bencher.fileResource
import ch.uzh.ifi.seal.bencher.measurement.PerformanceChangesTestHelper
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizerTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.uma.jmetal.solution.permutationsolution.PermutationSolution
import org.uma.jmetal.util.aggregationfunction.impl.WeightedSum

class GreedyTest {

    private lateinit var benchs: List<Benchmark>
    private lateinit var benchmarkIdMap: BenchmarkIdMap
    private lateinit var cov: Coverages
    private lateinit var weights: CoverageUnitWeights
    private lateinit var covObjective: CoverageObjective
    private lateinit var covOverlapObjective: CoverageOverlapObjective
    private lateinit var changeHistoryObjective: ChangeHistoryObjective

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

    private fun aggregate(objectives: List<Objective>, normalize: Boolean = true): Aggregation = Aggregation(
        function = WeightedSum(normalize),
        weights = objectives.indices.map { 1.0 / objectives.size }.toDoubleArray(),
        objectives = if (!normalize) { null } else { objectives },
    )

    private fun greedyAlgorithm(objectives: List<Objective>, aggregate: Aggregation): Greedy {
        val problem = PrioritizationProblem(benchmarkIdMap, objectives)
        return Greedy(problem, benchmarkIdMap, objectives, aggregate)
    }

    private fun runGreedyCheckResults(objectives: List<Objective>, normalize: Boolean): List<PrioritizedMethod<Benchmark>> {
        val aggregate = aggregate(objectives, normalize)

        val a = greedyAlgorithm(objectives, aggregate)
        a.run()
        val s = a.result()

        val results = JMetalPrioritizer.transformSolutions(benchmarkIdMap, listOf(s))
            .getOrElse { Assertions.fail("could not transform solutions: $it") }

        Assertions.assertEquals(1, results.size)

        val result = results[0]

        Assertions.assertEquals(4, result.size)

        return result
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    private fun oneObjectiveEqualWeightsCoverage(normalize: Boolean) {
        val objectives: List<Objective> = listOf(covObjective)

        val result = runGreedyCheckResults(objectives, normalize)

        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, result[0].method)
        Assertions.assertEquals(JarTestHelper.BenchParameterized2.bench4, result[1].method)
        Assertions.assertEquals(JarTestHelper.OtherBench.bench3, result[2].method)
        Assertions.assertEquals(JarTestHelper.BenchNonParameterized.bench2, result[3].method)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    private fun oneObjectiveEqualWeightsCoverageOverlap(normalize: Boolean) {
        val objectives: List<Objective> = listOf(covOverlapObjective)

        val result = runGreedyCheckResults(objectives, normalize)

        Assertions.assertEquals(JarTestHelper.BenchParameterized2.bench4, result[0].method)
        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, result[1].method)
        Assertions.assertEquals(JarTestHelper.BenchNonParameterized.bench2, result[2].method)
        Assertions.assertEquals(JarTestHelper.OtherBench.bench3, result[3].method)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    private fun oneObjectiveEqualWeightsCoverageChangeHistory(normalize: Boolean) {
        val objectives: List<Objective> = listOf(changeHistoryObjective)

        val result = runGreedyCheckResults(objectives, normalize)

        Assertions.assertEquals(JarTestHelper.BenchParameterized2.bench4, result[0].method)
        Assertions.assertEquals(JarTestHelper.OtherBench.bench3, result[1].method)
        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, result[2].method)
        Assertions.assertEquals(JarTestHelper.BenchNonParameterized.bench2, result[3].method)
    }

    @Test
    fun threeObjectivesEqualWeights() {
        val objectives: List<Objective> = listOf(covObjective, covOverlapObjective, changeHistoryObjective)

        val result = runGreedyCheckResults(objectives, true)

        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, result[0].method)
        Assertions.assertEquals(JarTestHelper.BenchParameterized2.bench4, result[1].method)
        Assertions.assertEquals(JarTestHelper.OtherBench.bench3, result[2].method)
        Assertions.assertEquals(JarTestHelper.BenchNonParameterized.bench2, result[3].method)
    }

    private fun comp(): Comparator<PermutationSolution<Int>> = AggregateObjectiveComparator(Aggregation(
        function = WeightedSum(false),
        weights = doubleArrayOf(0.5, 0.5),
    ))

    companion object {
        val distanceFile: String = "kroA100.tsp".fileResource().absolutePath
        val costFile: String = "kroB100.tsp".fileResource().absolutePath
    }
}
