package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.NotCovered
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CallGraphTest {

    @Test
    fun mergeEmpty() {
        val cgResults: Iterable<CGResult> = listOf()
        val merged = cgResults.merge()
        Assertions.assertEquals(CGResult(mapOf()), merged)
    }

    @Test
    fun mergeOne() {
        val cgRes = CGResult(mapOf())
        val cgResults = listOf(cgRes)
        val merged = cgResults.merge()
        val expected = CGResult(mapOf())
        Assertions.assertEquals(expected, merged)
    }

    @Test
    fun mergeSingle() {
        val cgRes = CGResult(mapOf(b1Cg, b2Cg, b3Cg))
        val cgResults = listOf(cgRes)
        val merged = cgResults.merge()
        Assertions.assertEquals(expectedCgResult, merged)
    }

    @Test
    fun mergeMultiDisjoint() {
        val cgRes1 = CGResult(mapOf(b1Cg))
        val cgRes2 = CGResult(mapOf(b2Cg))
        val cgRes3 = CGResult(mapOf(b3Cg))
        val cgResults = listOf(cgRes1, cgRes2, cgRes3)
        val merged = cgResults.merge()
        Assertions.assertEquals(expectedCgResult, merged)
    }

    @Test
    fun mergeMultiOverlapping() {
        val cgRes1 = CGResult(mapOf(b1Cg, b2Cg))
        val cgRes2 = CGResult(mapOf(b2Cg, b3Cg))
        val cgRes3 = CGResult(mapOf(b1Cg, b3Cg))
        val cgResults = listOf(cgRes1, cgRes2, cgRes3)
        val merged = cgResults.merge()

        Assertions.assertEquals(expectedCgResult, merged)

        expectedCgResult.calls.forEach { (b, calls) ->
            Assertions.assertTrue(merged.calls.containsKey(b), "Merged CGResult does not contain benchmark $b")

            val mergedCalls = merged.calls[b]
            Assertions.assertNotNull(mergedCalls)

            calls.all().forEach { c ->
                val cc = mergedCalls!!.all().contains(c)
                Assertions.assertTrue(cc, "Merged CGResult for bench ($b) does not contain MethodCall ($c)")
            }
        }
    }

    @Test
    fun reachable() {
        val cg = CGResult(mapOf(b1Cg))
        val ra = cg.single(JarTestHelper.BenchParameterized.bench1, JarTestHelper.CoreA.m)
        Assertions.assertFalse(ra is NotCovered)
        val rb = cg.single(JarTestHelper.BenchParameterized.bench1, JarTestHelper.CoreB.m)
        Assertions.assertFalse(rb is NotCovered)
    }

    @Test
    fun notReachable() {
        val cg = CGResult(mapOf(b1Cg))
        val rd = cg.single(JarTestHelper.BenchParameterized.bench1, JarTestHelper.CoreD.m)
        Assertions.assertTrue(rd is NotCovered)
    }

    @Test
    fun multipleReachable() {
        val cg = CGResult(mapOf(b1Cg))

        listOf(JarTestHelper.CoreA.m, JarTestHelper.CoreB.m).forEach { to ->
            val r = cg.single(JarTestHelper.BenchParameterized.bench1, to)
            Assertions.assertFalse(r is NotCovered)
        }
    }

    @Test
    fun multipleNotReachable() {
        val cg = CGResult(mapOf(b2Cg))

        listOf(JarTestHelper.CoreA.m, JarTestHelper.CoreB.m).forEach { to ->
            var r = cg.single(JarTestHelper.BenchParameterized.bench1, to)
            Assertions.assertTrue(r is NotCovered)
        }
    }

    companion object {
        private val b1Cg = CGTestHelper.b1Cg
        private val b2Cg = CGTestHelper.b2Cg
        private val b3Cg = CGTestHelper.b3Cg
        private val expectedCgResult = CGResult(mapOf(b1Cg, b2Cg, b3Cg))
    }
}
