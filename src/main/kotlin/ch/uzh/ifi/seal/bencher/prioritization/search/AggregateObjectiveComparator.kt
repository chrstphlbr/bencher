package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.solution.Solution
import java.io.Serializable

class AggregateObjectiveComparator<S>(
    private val aggregation: Aggregation,
    private val ascending: Boolean = true,
) : Comparator<S>, Serializable where S : Solution<*> {

    override fun compare(s1: S, s2: S): Int {
        checkPreconditions(s1, s2)

        val o1 = aggregateObjectives(s1)
        val o2 = aggregateObjectives(s2)

        return if (ascending) {
            o1.compareTo(o2)
        } else {
            o2.compareTo(o1)
        }
    }

    private fun checkPreconditions(s1: S, s2: S) {
        val s1ObjectivesSize = s1.objectives().size
        val s2ObjectivesSize = s2.objectives().size

        if (s1ObjectivesSize != s2ObjectivesSize) {
            throw IllegalArgumentException("s1 ($s1ObjectivesSize) and s2 ($s2ObjectivesSize) objectives sizes must be equal")
        }
    }

    private fun aggregateObjectives(s: S): Double = aggregation.compute(s.objectives())
}
