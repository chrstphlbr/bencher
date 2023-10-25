package ch.uzh.ifi.seal.bencher.measurement

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random

class MeanTest {
    private lateinit var r: Random

    @BeforeEach
    fun setup() {
        r = Random(System.currentTimeMillis())
    }

    @Test
    fun name() {
        Assertions.assertEquals("mean", Mean.name)
    }

    @Test
    fun empty() {
        Assertions.assertEquals(0.0, Mean.statistic(listOf()))
    }

    @Test
    fun one() {
        repeat(REPETITIONS) {
            val v = r.nextInt(from = MIN, until = MAX)
            Assertions.assertEquals(v.toDouble(), Mean.statistic(listOf(v)))
        }
    }

    @Test
    fun many() {
        repeat(REPETITIONS) {
            val nrElements = r.nextInt(from = 2,until = MAX_ELEMENTS)
            val elements = (0 until nrElements).map { r.nextInt(from = MIN, until = MAX) }

            val sum = elements.sum() * 1.0 / elements.size

            Assertions.assertEquals(sum, Mean.statistic(elements))
        }
    }

    companion object {
        private const val REPETITIONS = 100
        private const val MAX = 1_000_000
        private const val MIN = MAX * -1
        private const val MAX_ELEMENTS = 100
    }
}
