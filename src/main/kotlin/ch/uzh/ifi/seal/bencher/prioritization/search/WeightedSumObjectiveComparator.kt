package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.solution.Solution
import java.io.Serializable
import kotlin.math.absoluteValue

class WeightedSumObjectiveComparator<S>(
    private val weights: List<Double>, // the sum of the weights must be 1.0
    private val ascending: Boolean = true,
) : Comparator<S>, Serializable where S : Solution<*> {

    private val acceptableRoundingError = 0.00001

    init {
        if (weights.isEmpty()) {
            throw IllegalArgumentException("expected at least one weight")
        }

        val sumNotOk = (weights.sum() - 1.0).absoluteValue > acceptableRoundingError
        if (sumNotOk) {
            throw IllegalArgumentException("weights sum must be 1.0")
        }
    }

    override fun compare(s1: S, s2: S): Int {
        checkPrecondidtions(s1, s2)

        val o1 = weightedObjective(s1)
        val o2 = weightedObjective(s2)

        return if (ascending) {
            o1.compareTo(o2)
        } else {
            o2.compareTo(o1)
        }
    }

    private fun checkPrecondidtions(s1: S, s2: S) {
        checkPrecondidtions(s1, 1)
        checkPrecondidtions(s2, 2)
    }

    private fun checkPrecondidtions(s: S, solutionNumber: Int) {
        val solutionObjectivesSize = s.objectives().size
        if (solutionObjectivesSize != weights.size) {
            throw IllegalArgumentException("The s$solutionNumber has $solutionObjectivesSize objectives but the number of weights is ${weights.size}")
        }
    }

    private fun weightedObjective(s: S): Double = weights.mapIndexed { i, w -> w * s.objectives()[i] }.sum()
}
