package ch.uzh.ifi.seal.bencher.prioritization.search

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizerTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.uma.jmetal.solution.permutationsolution.PermutationSolution
import org.uma.jmetal.solution.permutationsolution.impl.IntegerPermutationSolution

class JMetalPrioritizerTransformSolutionsTest {

    @Test
    fun empty() {
        val solutions = listOf<PermutationSolution<Int>>()
        val results = JMetalPrioritizer.transformSolutions(idMap, solutions).getOrElse { Assertions.fail() }
        Assertions.assertTrue(results.isEmpty())
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3, 4, 10])
    fun many(nrSolutions: Int) {
        val randomSolutions = (0 until nrSolutions).map {
            randomSolution(benchs, idMap)
        }

        val solutions = randomSolutions.map { it.solution }

        val results = JMetalPrioritizer.transformSolutions(idMap, solutions).getOrElse { Assertions.fail() }
        Assertions.assertEquals(solutions.size, results.size)

        results.forEachIndexed { resultIdx, prioritizedBenchs ->
            Assertions.assertEquals(benchs.size, prioritizedBenchs.size)
            prioritizedBenchs.forEachIndexed { i, b ->
                Assertions.assertEquals(randomSolutions[resultIdx].benchs[i], b.method)
            }
        }
    }

    private fun randomSolution(benchs: List<Benchmark>, idMap: BenchmarkIdMap): RandomSolution {
        val solution = IntegerPermutationSolution(benchs.size, 2, 0)

        val randomBenchs = benchs.shuffled()

        randomBenchs.forEachIndexed { i, b ->
            solution.variables()[i] = idMap[b] ?: Assertions.fail("could not get id for benchmark $b")
        }

        return RandomSolution(randomBenchs, solution)
    }

    private class RandomSolution(
        val benchs: List<Benchmark>,
        val solution: PermutationSolution<Int>,
    )

    companion object {
        private lateinit var benchs: List<Benchmark>
        private lateinit var idMap: BenchmarkIdMap

        @BeforeAll
        @JvmStatic
        fun setUpClass() {
            val benchSize = 4
            benchs = PrioritizerTestHelper.benchs
            Assertions.assertEquals(benchSize, benchs.size)
            idMap = BenchmarkIdMapImpl(PrioritizerTestHelper.benchs)
            Assertions.assertEquals(benchSize, idMap.size)
        }
    }
}
