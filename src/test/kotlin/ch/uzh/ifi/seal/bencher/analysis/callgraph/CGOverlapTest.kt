package ch.uzh.ifi.seal.bencher.analysis.callgraph

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CGOverlapTest {

    @Test
    fun nonoverlappingCallgraphsTwoBenchs() {
        val b1 = b3rs.first
        val rs1 = b3rs.second
        val b2 = b4rs.first
        val rs2 = b4rs.second

        val overlap = CGOverlapImpl(listOf(rs1, rs2))

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
    fun overlappingCallgraphTwoBenchsOneMeth() {
        val b1 = b1rs.first
        val rs1 = b1rs.second
        val b2 = b2rs.first
        val rs2 = b2rs.second

        val overlap = CGOverlapImpl(listOf(rs1, rs2))

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
    fun overlappingCallgraphTwoBenchsTwoMeths() {
        val b1 = b1rs.first
        val rs1 = b1rs.second
        val b2 = b3rs.first
        val rs2 = b3rs.second

        val overlap = CGOverlapImpl(listOf(rs1, rs2))

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
    fun overlappingCallgraphFourBenchs() {
        val b1 = b1rs.first
        val rs1 = b1rs.second
        val b2 = b2rs.first
        val rs2 = b2rs.second
        val b3 = b3rs.first
        val rs3 = b3rs.second
        val b4 = b4rs.first
        val rs4 = b4rs.second

        val overlap = CGOverlapImpl(listOf(rs1, rs2, rs3, rs4))

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
        private val b1rs = CGTestHelper.b1Cg
        private val b2rs = CGTestHelper.b2Cg
        private val b3rs = CGTestHelper.b3Cg
        private val b4rs = CGTestHelper.b4Cg
    }
}
