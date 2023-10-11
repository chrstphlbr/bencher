package ch.uzh.ifi.seal.bencher.prioritization.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.math.pow
import kotlin.math.round

class ObjectiveFunctionAveragePercentageTest {

    private fun assertEqualsPrecision(expected: Double, actual: Double, precision: Int = 5) {
        val precisionFactor = 10.0.pow(precision)
        val rounded = round(actual*precisionFactor)/precisionFactor
        Assertions.assertEquals(expected, rounded)
    }

    @Test
    fun noValues() {
        val values = listOf<Double>()

        val rDefault = AveragePercentage().compute(values)
        Assertions.assertEquals(-1.0, rDefault)

        val exp = -321.0
        val r = AveragePercentage(defaultEmptyList = exp).compute(values)
        Assertions.assertEquals(exp, r)
    }

    @Test
    fun zeroSum() {
        val values = listOf(0.0, 0.0, 0.0, 0.0, 0.0)

        val rDefault = AveragePercentage().compute(values)
        Assertions.assertEquals(-2.0, rDefault)

        val exp = -123.0
        val r = AveragePercentage(defaultListSumZero = exp).compute(values)
        Assertions.assertEquals(exp, r)
    }

    @Test
    fun maximum() {
        val values = listOf(5.0, 4.0, 3.0, 2.0, 1.0)

        val r = AveragePercentage().compute(values)
        assertEqualsPrecision(0.73333, r)
    }

    @Test
    fun minimum() {
        val values = listOf(1.0, 2.0, 3.0, 4.0, 5.0)

        val r = AveragePercentage().compute(values)
        assertEqualsPrecision(0.46667, r)
    }

    @Test
    fun middle() {
        val values = listOf(3.0, 5.0, 1.0, 2.0, 4.0)

        val r = AveragePercentage().compute(values)
        assertEqualsPrecision(0.61333, r)
    }
}
