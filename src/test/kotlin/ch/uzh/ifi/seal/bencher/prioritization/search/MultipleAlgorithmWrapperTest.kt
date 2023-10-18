package ch.uzh.ifi.seal.bencher.prioritization.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.uma.jmetal.algorithm.Algorithm
import org.uma.jmetal.solution.Solution

class MultipleAlgorithmWrapperTest {
    @Test
    fun algorithmNotRun() {
        val a = MultipleAlgorithmWrapper(listOf(AlgorithmTestHelper.AlgorithmMultipleResultsMock(1)))
        val results = a.result()
        Assertions.assertEquals(0, results.size)
    }

    @Test
    fun noAlgorithm() {
        val algorithms = listOf<Algorithm<List<Solution<Int>>>>()
        val a = MultipleAlgorithmWrapper(algorithms)
        a.run()
        val results = a.result()
        Assertions.assertEquals(0, results.size)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 5, 10])
    fun singleAlgorithmMultipleResults(size: Int) {
        val a = MultipleAlgorithmWrapper(listOf(AlgorithmTestHelper.AlgorithmMultipleResultsMock(size)))
        a.run()
        val results = a.result()
        Assertions.assertEquals(size, results.size)

        (0 until size).forEach { i ->
            val result = results[i]
            Assertions.assertEquals(0, result.objectives().size)
            Assertions.assertEquals(0, result.constraints().size)
            Assertions.assertEquals(1, result.variables().size)
            Assertions.assertEquals(i+1, result.variables()[0])
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 5, 10])
    fun multipleAlgorithmsSingleResult(size: Int) {
        val algorithms = (0 until size).map { i ->
            MultipleSolutionsAlgorithmWrapper(AlgorithmTestHelper.AlgorithmSingleResultMock(i+1))
        }
        val a = MultipleAlgorithmWrapper(algorithms)
        a.run()
        val results = a.result()
        Assertions.assertEquals(size, results.size)

        (0 until size).forEach { i ->
            val result = results[i]
            Assertions.assertEquals(0, result.objectives().size)
            Assertions.assertEquals(0, result.constraints().size)
            Assertions.assertEquals(1, result.variables().size)
            Assertions.assertEquals(i+1, result.variables()[0])
        }
    }

    @ParameterizedTest
    @CsvSource(value = [
        "2, 2",
        "2, 5",
        "5, 2",
        "5, 5",
    ], ignoreLeadingAndTrailingWhitespace = true)
    fun multipleAlgorithmsMultipleResults(size: Int, variables: Int) {
        val algorithms = (0 until size).map {
            AlgorithmTestHelper.AlgorithmMultipleResultsMock(variables)
        }
        val a = MultipleAlgorithmWrapper(algorithms)
        a.run()
        val results = a.result()

        val expectedResultsSize = size * variables
        Assertions.assertEquals(expectedResultsSize, results.size)

        (0 until expectedResultsSize).forEach { i ->
            val variableValue = i % variables + 1
            val result = results[i]
            Assertions.assertEquals(0, result.objectives().size)
            Assertions.assertEquals(0, result.constraints().size)
            Assertions.assertEquals(1, result.variables().size)
            Assertions.assertEquals(variableValue, result.variables()[0])
        }
    }
}
