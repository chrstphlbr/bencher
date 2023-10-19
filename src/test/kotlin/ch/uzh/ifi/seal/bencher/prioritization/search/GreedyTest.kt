package ch.uzh.ifi.seal.bencher.prioritization.search

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.uma.jmetal.problem.permutationproblem.impl.FakeIntegerPermutationProblem

class GreedyTest : AlgorithmTest() {

    private fun greedyAlgorithm(objectives: List<Objective>, aggregate: Aggregation?): Greedy {
        val problem = PrioritizationProblem(benchmarkIdMap, objectives)
        return Greedy(problem, benchmarkIdMap, objectives, aggregate)
    }

    private fun runGreedyCheckResult(objectives: List<Objective>, normalize: Boolean): List<PrioritizedMethod<Benchmark>> {
        val aggregate = if (objectives.size != 1) {
            aggregation(objectives, normalize)
        } else {
            null
        }
        val a = greedyAlgorithm(objectives, aggregate)
        return runAlgorithmCheckResult(a)
    }

    @Test
    fun invalidVariablesProblem() {
        val problem = FakeIntegerPermutationProblem(0, 0)

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Greedy(problem, benchmarkIdMap, listOf(), null)
        }
    }

    @Test
    fun invalidVariablesBenchmarks() {
        val problem = FakeIntegerPermutationProblem(10, 0)
        val benchmarkIdMap = BenchmarkIdMapImpl(listOf())

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Greedy(problem, benchmarkIdMap, listOf(), null)
        }
    }

    @Test
    fun invalidVariablesProblemBenchmarksDiff() {
        val problem = FakeIntegerPermutationProblem(benchmarkIdMap.size + 1, 0)

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Greedy(problem, benchmarkIdMap, listOf(), null)
        }
    }

    @Test
    fun emptyObjectives() {
        val problem = FakeIntegerPermutationProblem(benchmarkIdMap.size, 0)
        val objectives: List<Objective> = listOf()

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Greedy(problem, benchmarkIdMap, objectives, null)
        }
    }

    @Test
    fun oneObjectiveAggregationNotNull() {
        val problem = FakeIntegerPermutationProblem(benchmarkIdMap.size, 1)
        val objectives: List<Objective> = listOf(covObjective)
        val aggregation = aggregation(objectives, false)

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Greedy(problem, benchmarkIdMap, objectives, aggregation)
        }
    }

    @Test
    fun multipleObjectivesAggregationNull() {
        val problem = FakeIntegerPermutationProblem(benchmarkIdMap.size, 3)
        val objectives: List<Objective> = listOf(covObjective, covOverlapObjective, changeHistoryObjective)

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            Greedy(problem, benchmarkIdMap, objectives, null)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun oneObjectiveEqualWeightsCoverage(normalize: Boolean) {
        val objectives: List<Objective> = listOf(covObjective)

        val result = runGreedyCheckResult(objectives, normalize)

        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, result[0].method)
        Assertions.assertEquals(JarTestHelper.BenchParameterized2.bench4, result[1].method)
        Assertions.assertEquals(JarTestHelper.OtherBench.bench3, result[2].method)
        Assertions.assertEquals(JarTestHelper.BenchNonParameterized.bench2, result[3].method)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun oneObjectiveEqualWeightsCoverageOverlap(normalize: Boolean) {
        val objectives: List<Objective> = listOf(covOverlapObjective)

        val result = runGreedyCheckResult(objectives, normalize)

        Assertions.assertEquals(JarTestHelper.BenchParameterized2.bench4, result[0].method)
        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, result[1].method)
        Assertions.assertEquals(JarTestHelper.BenchNonParameterized.bench2, result[2].method)
        Assertions.assertEquals(JarTestHelper.OtherBench.bench3, result[3].method)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun oneObjectiveEqualWeightsCoverageChangeHistory(normalize: Boolean) {
        val objectives: List<Objective> = listOf(changeHistoryObjective)

        val result = runGreedyCheckResult(objectives, normalize)

        Assertions.assertEquals(JarTestHelper.BenchParameterized2.bench4, result[0].method)
        Assertions.assertEquals(JarTestHelper.OtherBench.bench3, result[1].method)
        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, result[2].method)
        Assertions.assertEquals(JarTestHelper.BenchNonParameterized.bench2, result[3].method)
    }

    @Test
    fun threeObjectivesEqualWeights() {
        val objectives: List<Objective> = listOf(covObjective, covOverlapObjective, changeHistoryObjective)

        val result = runGreedyCheckResult(objectives, true)

        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, result[0].method)
        Assertions.assertEquals(JarTestHelper.BenchParameterized2.bench4, result[1].method)
        Assertions.assertEquals(JarTestHelper.OtherBench.bench3, result[2].method)
        Assertions.assertEquals(JarTestHelper.BenchNonParameterized.bench2, result[3].method)
    }
}
