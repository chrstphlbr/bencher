package ch.uzh.ifi.seal.bencher.prioritization.search

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.uma.jmetal.problem.permutationproblem.PermutationProblem
import org.uma.jmetal.solution.permutationsolution.PermutationSolution

class HillClimbingTest : AlgorithmTest() {

    private fun comparator(objectives: List<Objective>, normalize: Boolean): Comparator<PermutationSolution<Int>> =
        AggregateObjectiveComparator(aggregation(objectives, normalize))

    private fun prepareInitialSolution(
        benchs: List<Benchmark>,
        p: PermutationProblem<PermutationSolution<Int>>,
    ): PermutationSolution<Int> {
        val fixedInitialSolution = p.createSolution()
        benchs.forEachIndexed { i, b ->
            val id = benchmarkIdMap[b]!!
            fixedInitialSolution.variables()[i] = id
        }

        return p.evaluate(fixedInitialSolution)
    }

    private fun runHillClimbing(benchs: List<Benchmark>): List<PrioritizedMethod<Benchmark>> {
        val objectives = listOf(covObjective, covOverlapObjective, changeHistoryObjective)

        val p = PrioritizationProblem(benchmarkIdMap, objectives)
        val comp = comparator(objectives, false)

        val initial = prepareInitialSolution(benchs, p)

        val a = HillClimbing(initial, p, comp, PermutationNeighborhood(), MAX_EVALUATIONS)

        return runAlgorithmCheckResult(a)
    }

    @Test
    fun benchOrder() {
        val result = runHillClimbing(benchs)

        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, result[0].method)
        Assertions.assertEquals(JarTestHelper.BenchNonParameterized.bench2, result[1].method)
        Assertions.assertEquals(JarTestHelper.OtherBench.bench3, result[2].method)
        Assertions.assertEquals(JarTestHelper.BenchParameterized2.bench4, result[3].method)
    }

    @Test
    fun reverseBenchOrder() {
        val result = runHillClimbing(benchs.reversed())

        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, result[0].method)
        Assertions.assertEquals(JarTestHelper.OtherBench.bench3, result[1].method)
        Assertions.assertEquals(JarTestHelper.BenchNonParameterized.bench2, result[2].method)
        Assertions.assertEquals(JarTestHelper.BenchParameterized2.bench4, result[3].method)
    }

    @Test
    fun orderBenchmarks2314() {
        val benchOrder = listOf(benchs[1], benchs[2], benchs[0], benchs[3])
        val result = runHillClimbing(benchOrder)

        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, result[0].method)
        Assertions.assertEquals(JarTestHelper.OtherBench.bench3, result[1].method)
        Assertions.assertEquals(JarTestHelper.BenchParameterized2.bench4, result[2].method)
        Assertions.assertEquals(JarTestHelper.BenchNonParameterized.bench2, result[3].method)
    }

    @Test
    fun orderBenchmarks3241() {
        val benchOrder = listOf(benchs[2], benchs[1], benchs[3], benchs[0])
        val result = runHillClimbing(benchOrder)

        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, result[0].method)
        Assertions.assertEquals(JarTestHelper.BenchNonParameterized.bench2, result[1].method)
        Assertions.assertEquals(JarTestHelper.BenchParameterized2.bench4, result[2].method)
        Assertions.assertEquals(JarTestHelper.OtherBench.bench3, result[3].method)
    }

    companion object {
        private const val MAX_EVALUATIONS = 100
    }
}
