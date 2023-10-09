package ch.uzh.ifi.seal.bencher.prioritization.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.uma.jmetal.solution.permutationsolution.impl.IntegerPermutationSolution
import kotlin.properties.Delegates
import kotlin.random.Random

class PermutationNeighborhoodTest {
    private lateinit var neighborhood: PermutationNeighborhood<Int>
    private var permutationLength by Delegates.notNull<Int>()

    @BeforeEach
    fun setUp() {
        neighborhood = PermutationNeighborhood()
        val r = Random(System.currentTimeMillis())
        permutationLength = r.nextInt(MAX_LENGTH)
    }

    @Test
    fun empty() {
        Assertions.assertThrows(
            IllegalArgumentException::class.java,
            { neighborhood.getNeighbors(listOf(), 0) },
            "expected an exception because there are no neighbors for an empty list"
        )
    }

    @Test
    fun invalidIndex() {
        val s = IntegerPermutationSolution(permutationLength, 0, 0)
        Assertions.assertThrows(
            IllegalArgumentException::class.java,
            { neighborhood.getNeighbors(listOf(s), 1) },
            "expected an exception because of an invalid index"
        )
    }

    @Test
    fun oneVariable() {
        val s = IntegerPermutationSolution(1, 0, 0)
        val ns = neighborhood.getNeighbors(listOf(s), 0)
        Assertions.assertTrue(ns.isEmpty(), "expected 0 neighbors")
    }

    @Test
    fun multipleVariables() {
        val length = permutationLength
        val s = IntegerPermutationSolution(length, 0, 0)
        val ns = neighborhood.getNeighbors(listOf(s), 0)

        Assertions.assertEquals(length - 1, ns.size)

        val first = s.variables()[0]
        (1 until length).forEach { i ->
            val expectedNeighbor = s.copy()
            expectedNeighbor.variables()[0] = expectedNeighbor.variables()[i]
            expectedNeighbor.variables()[i] = first

            Assertions.assertTrue(
                ns.contains(expectedNeighbor),
                "expected neighbor not contained in neighbors: $expectedNeighbor",
            )
        }
    }

    companion object {
        private const val MAX_LENGTH = 10000
    }
}
