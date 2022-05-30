package ch.uzh.ifi.seal.bencher.analysis.coverage

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CoverageOverlapTest {

    @Test
    fun nonoverlappingCoveragesTwoBenchs() {
        val b1 = b3cov.first
        val cov1 = b3cov.second
        val b2 = b4cov.first
        val cov2 = b4cov.second

        val overlap = CoverageOverlapImpl(listOf(cov1, cov2))

        // B1

        val overlappingB1 = overlap.overlapping(b1, b2)
        Assertions.assertFalse(overlappingB1)

        val overlappingPercB1B2 = overlap.overlappingPercentage(b1, b2)
        Assertions.assertEquals(0.0, overlappingPercB1B2)

        val overlappingPercB1 = overlap.overlappingPercentage(b1)
        Assertions.assertEquals(0.0, overlappingPercB1)

        // B2

        val overlappingB2 = overlap.overlapping(b2, b1)
        Assertions.assertFalse(overlappingB2)

        val overlappingPercB2B1 = overlap.overlappingPercentage(b2, b1)
        Assertions.assertEquals(0.0, overlappingPercB2B1)

        val overlappingPercB2 = overlap.overlappingPercentage(b2)
        Assertions.assertEquals(0.0, overlappingPercB2)
    }

    @Test
    fun overlappingCoveragesTwoBenchsOneMeth() {
        val b1 = b1cov.first
        val cov1 = b1cov.second
        val b2 = b2cov.first
        val cov2 = b2cov.second

        val overlap = CoverageOverlapImpl(listOf(cov1, cov2))

        // B1

        val overlappingB1 = overlap.overlapping(b1, b2)
        Assertions.assertTrue(overlappingB1)

        val overlappingPercB1B2 = overlap.overlappingPercentage(b1, b2)
        Assertions.assertEquals(0.2, overlappingPercB1B2)

        val overlappingPercB1 = overlap.overlappingPercentage(b1)
        Assertions.assertEquals(0.2, overlappingPercB1)

        // B2

        val overlappingB2 = overlap.overlapping(b2, b1)
        Assertions.assertTrue(overlappingB2)

        val overlappingPercB2B1 = overlap.overlappingPercentage(b2, b1)
        Assertions.assertEquals(1.0, overlappingPercB2B1)

        val overlappingPercB2 = overlap.overlappingPercentage(b2)
        Assertions.assertEquals(1.0, overlappingPercB2)
    }

    @Test
    fun overlappingCoveragesTwoBenchsTwoMeths() {
        val b1 = b1cov.first
        val cov1 = b1cov.second
        val b2 = b3cov.first
        val cov2 = b3cov.second

        val overlap = CoverageOverlapImpl(listOf(cov1, cov2))

        // B1

        val overlappingB1 = overlap.overlapping(b1, b2)
        Assertions.assertTrue(overlappingB1)

        val overlappingPercB1B2 = overlap.overlappingPercentage(b1, b2)
        Assertions.assertEquals(0.4, overlappingPercB1B2)

        val overlappingPercB1 = overlap.overlappingPercentage(b1)
        Assertions.assertEquals(0.4, overlappingPercB1)

        // B2

        val overlappingB2 = overlap.overlapping(b2, b1)
        Assertions.assertTrue(overlappingB2)

        val overlappingPercB2B1 = overlap.overlappingPercentage(b2, b1)
        Assertions.assertEquals(1.0, overlappingPercB2B1)

        val overlappingPercB2 = overlap.overlappingPercentage(b2)
        Assertions.assertEquals(1.0, overlappingPercB2)
    }

    @Test
    fun overlappingCoveragesFourBenchs() {
        val b1 = b1cov.first
        val cov1 = b1cov.second
        val b2 = b2cov.first
        val cov2 = b2cov.second
        val b3 = b3cov.first
        val cov3 = b3cov.second
        val b4 = b4cov.first
        val cov4 = b4cov.second

        val overlap = CoverageOverlapImpl(listOf(cov1, cov2, cov3, cov4))

        // B1
        val b1Func = fun() {
            val overlappingB1B2 = overlap.overlapping(b1, b2)
            Assertions.assertTrue(overlappingB1B2)

            val overlappingB1B3 = overlap.overlapping(b1, b3)
            Assertions.assertTrue(overlappingB1B3)

            val overlappingB1B4 = overlap.overlapping(b1, b4)
            Assertions.assertTrue(overlappingB1B4)

            val overlappingPercB1B2 = overlap.overlappingPercentage(b1, b2)
            Assertions.assertEquals(0.2, overlappingPercB1B2)

            val overlappingPercB1B3 = overlap.overlappingPercentage(b1, b3)
            Assertions.assertEquals(0.4, overlappingPercB1B3)

            val overlappingPercB1B4 = overlap.overlappingPercentage(b1, b4)
            Assertions.assertEquals(0.2, overlappingPercB1B4)

            val overlappingPercB1 = overlap.overlappingPercentage(b1)
            Assertions.assertEquals(0.6, overlappingPercB1)
        }
        b1Func()

        // B2
        val b2Func = fun() {
            val overlappingB2B1 = overlap.overlapping(b2, b1)
            Assertions.assertTrue(overlappingB2B1)

            val overlappingB2B3 = overlap.overlapping(b2, b3)
            Assertions.assertTrue(overlappingB2B3)

            val overlappingB2B4 = overlap.overlapping(b2, b4)
            Assertions.assertFalse(overlappingB2B4)

            val overlappingPercB2B1 = overlap.overlappingPercentage(b2, b1)
            Assertions.assertEquals(1.0, overlappingPercB2B1)

            val overlappingPercB2B3 = overlap.overlappingPercentage(b2, b3)
            Assertions.assertEquals(1.0, overlappingPercB2B3)

            val overlappingPercB2B4 = overlap.overlappingPercentage(b2, b4)
            Assertions.assertEquals(0.0, overlappingPercB2B4)

            val overlappingPercB2 = overlap.overlappingPercentage(b2)
            Assertions.assertEquals(1.0, overlappingPercB2)
        }
        b2Func()

        // B3
        val b3Func = fun() {
            val overlappingB3B1 = overlap.overlapping(b3, b1)
            Assertions.assertTrue(overlappingB3B1)

            val overlappingB3B2 = overlap.overlapping(b3, b2)
            Assertions.assertTrue(overlappingB3B2)

            val overlappingB3B4 = overlap.overlapping(b3, b4)
            Assertions.assertFalse(overlappingB3B4)

            val overlappingPercB3B1 = overlap.overlappingPercentage(b3, b1)
            Assertions.assertEquals(1.0, overlappingPercB3B1)

            val overlappingPercB3B2 = overlap.overlappingPercentage(b3, b2)
            Assertions.assertEquals(0.5, overlappingPercB3B2)

            val overlappingPercB3B4 = overlap.overlappingPercentage(b3, b4)
            Assertions.assertEquals(0.0, overlappingPercB3B4)

            val overlappingPercB3 = overlap.overlappingPercentage(b3)
            Assertions.assertEquals(1.0, overlappingPercB3)
        }
        b3Func()

        // B4
        val b4Func = fun() {
            val overlappingB4B1 = overlap.overlapping(b4, b1)
            Assertions.assertTrue(overlappingB4B1)

            val overlappingB4B2 = overlap.overlapping(b4, b2)
            Assertions.assertFalse(overlappingB4B2)

            val overlappingB4B3 = overlap.overlapping(b4, b3)
            Assertions.assertFalse(overlappingB4B3)

            val overlappingPercB4B1 = overlap.overlappingPercentage(b4, b1)
            Assertions.assertEquals(0.5, overlappingPercB4B1)

            val overlappingPercB4B2 = overlap.overlappingPercentage(b4, b2)
            Assertions.assertEquals(0.0, overlappingPercB4B2)

            val overlappingPercB4B3 = overlap.overlappingPercentage(b4, b3)
            Assertions.assertEquals(0.0, overlappingPercB4B3)

            val overlappingPercB4 = overlap.overlappingPercentage(b4)
            Assertions.assertEquals(0.5, overlappingPercB4)
        }
        b4Func()
    }

    companion object {
        private val b1cov = CoveragesTestHelper.b1MethodCov
        private val b2cov = CoveragesTestHelper.b2MethodCov
        private val b3cov = CoveragesTestHelper.b3MethodCov
        private val b4cov = CoveragesTestHelper.b4MethodCov
    }
}
