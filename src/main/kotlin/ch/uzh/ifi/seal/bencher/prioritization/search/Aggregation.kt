package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.util.aggregationfunction.AggregationFunction
import org.uma.jmetal.util.point.impl.IdealPoint
import org.uma.jmetal.util.point.impl.NadirPoint

class Aggregation(
    private val function: AggregationFunction,
    private val weights: DoubleArray,
    private val idealPoint: IdealPoint?,
    private val nadirPoint: NadirPoint?,
) {
    fun compute(values: DoubleArray): Double {
        checkPrecondition(values, weights)
        return function.compute(values, weights, idealPoint, nadirPoint)
    }

    private fun checkPrecondition(values: DoubleArray, weights: DoubleArray) {
        if (values.size != weights.size) {
            throw IllegalArgumentException("Values (${values.size}) and weights (${weights.size}) size must be equal")
        }
    }
}
