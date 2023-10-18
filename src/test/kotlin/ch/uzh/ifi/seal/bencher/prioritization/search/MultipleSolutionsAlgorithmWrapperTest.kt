package ch.uzh.ifi.seal.bencher.prioritization.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MultipleSolutionsAlgorithmWrapperTest {
    @Test
    fun algorithmNotRun() {
        val a = MultipleSolutionsAlgorithmWrapper(AlgorithmTestHelper.AlgorithmSingleResultMock(1))
        Assertions.assertThrows(UninitializedPropertyAccessException::class.java) { a.result() }
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 5, 10])
    fun singleResult(variableValue: Int) {
        val a = MultipleSolutionsAlgorithmWrapper(AlgorithmTestHelper.AlgorithmSingleResultMock(variableValue))
        a.run()
        val results = a.result()
        Assertions.assertEquals(1, results.size)

        val result = results[0]
        Assertions.assertEquals(0, result.objectives().size)
        Assertions.assertEquals(0, result.constraints().size)
        Assertions.assertEquals(1, result.variables().size)
        Assertions.assertEquals(variableValue, result.variables()[0])
    }
}
