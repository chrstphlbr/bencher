package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.util.aggregationfunction.AggregationFunction
import org.uma.jmetal.util.point.impl.IdealPoint
import org.uma.jmetal.util.point.impl.NadirPoint

class Aggregation(
    private val function: AggregationFunction,
    private val weights: DoubleArray,
    objectives: List<Objective>? = null,
) {

    private val idealPoint = IdealPoint(weights.size)
    private val nadirPoint = NadirPoint(weights.size)

    init {
        if (objectives != null) {
            assert(weights.size == objectives.size)

            val idealNadirs = objectives.map { o ->
                val min = Objective.toMinimization(o.type, o.minIndividual)
                val max = Objective.toMinimization(o.type, o.maxIndividual)
                // assumes minimization problem
                if (min < max) {
                    Pair(min, max)
                } else {
                    Pair(max, min)
                }
            }

            val ideals = idealNadirs.map { it.first }.toDoubleArray()
            val nadirs = idealNadirs.map { it.second }.toDoubleArray()

            idealPoint.update(ideals)
            nadirPoint.update(nadirs)
        }
    }

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
