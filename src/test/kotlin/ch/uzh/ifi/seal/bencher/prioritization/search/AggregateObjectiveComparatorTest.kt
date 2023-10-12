package ch.uzh.ifi.seal.bencher.prioritization.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.uma.jmetal.solution.Solution
import org.uma.jmetal.solution.permutationsolution.impl.IntegerPermutationSolution
import org.uma.jmetal.util.aggregationfunction.impl.WeightedSum
import kotlin.properties.Delegates
import kotlin.random.Random

class AggregateObjectiveComparatorTest {

    private var permutationLength by Delegates.notNull<Int>()
    private var numberOfConstraints by Delegates.notNull<Int>()

    @BeforeEach
    fun setUp() {
        val r = Random(System.currentTimeMillis())
        permutationLength = r.nextInt(MAX_LENGTH)
        numberOfConstraints = r.nextInt(MAX_CONSTRAINTS)
    }

    @Test
    fun unequalObjectiveNumbers() {
        val c = AggregateObjectiveComparator<IntegerPermutationSolution>(aggregation(doubleArrayOf(0.25, 0.5, 0.25)))

        val s1 = IntegerPermutationSolution(permutationLength, 3, numberOfConstraints)
        val s2 = IntegerPermutationSolution(permutationLength, 2, numberOfConstraints)

        Assertions.assertThrows(
            IllegalArgumentException::class.java,
            { c.compare(s1, s2) },
            "expected an exception due to different number of objectives"
        )
    }

    @Test
    fun unequalWeightsAndObjectives() {
        val c = AggregateObjectiveComparator<IntegerPermutationSolution>(aggregation(doubleArrayOf(0.25, 0.5, 0.25)))

        val s1 = IntegerPermutationSolution(permutationLength, 2, numberOfConstraints)
        val s2 = IntegerPermutationSolution(permutationLength, 2, numberOfConstraints)

        Assertions.assertThrows(
            IllegalArgumentException::class.java,
            { c.compare(s1, s2) },
            "expected an exception due to a difference between number of objectives and weights"
        )
    }

    private fun aggregation(weights: DoubleArray): Aggregation = Aggregation(
        function = WeightedSum(false),
        weights = weights,
        null,
    )

    private fun equalWeights(n: Int): DoubleArray = (0 until n).map { 1.0 / n }.toDoubleArray()

    private fun setObjectives(s: Solution<*>, n: Int, offset: Double) {
        (0 until n).forEach { i ->
            s.objectives()[i] = i + offset
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 3, 5, 10])
    fun less(numberOfObjectives: Int) {
        val weights = equalWeights(numberOfObjectives)

        val c = AggregateObjectiveComparator<IntegerPermutationSolution>(aggregation(weights))

        val s1 = IntegerPermutationSolution(permutationLength, numberOfObjectives, numberOfConstraints)
        setObjectives(s1, numberOfObjectives, 0.0)
        val s2 = IntegerPermutationSolution(permutationLength, numberOfObjectives, numberOfConstraints)
        setObjectives(s2, numberOfObjectives, 0.1)

        val comparison = c.compare(s1, s2)

        Assertions.assertTrue(comparison < 0)
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 3, 5, 10])
    fun equal(numberOfObjectives: Int) {
        val weights = equalWeights(numberOfObjectives)

        val c = AggregateObjectiveComparator<IntegerPermutationSolution>(aggregation(weights))

        val s1 = IntegerPermutationSolution(permutationLength, numberOfObjectives, numberOfConstraints)
        setObjectives(s1, numberOfObjectives, 0.0)
        val s2 = IntegerPermutationSolution(permutationLength, numberOfObjectives, numberOfConstraints)
        setObjectives(s2, numberOfObjectives, 0.0)

        val comparison = c.compare(s1, s2)

        Assertions.assertEquals(0, comparison)
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 3, 5, 10])
    fun greater(numberOfObjectives: Int) {
        val weights = equalWeights(numberOfObjectives)

        val c = AggregateObjectiveComparator<IntegerPermutationSolution>(aggregation(weights))

        val s1 = IntegerPermutationSolution(permutationLength, numberOfObjectives, numberOfConstraints)
        setObjectives(s1, numberOfObjectives, 1.0)
        val s2 = IntegerPermutationSolution(permutationLength, numberOfObjectives, numberOfConstraints)
        setObjectives(s2, numberOfObjectives, 0.0)

        val comparison = c.compare(s1, s2)

        Assertions.assertTrue(comparison > 0)
    }

    companion object {
        private const val MAX_LENGTH = 100
        private const val MAX_CONSTRAINTS = 100
    }
}
