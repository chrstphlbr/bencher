package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CallGraphTest {
    @Test
    fun mergeSingle() {
        val cgRes = CGResult(mapOf(b1Cg, b2Cg, b3Cg))
        val cgResults = listOf(cgRes)
        val merged = cgResults.merge()
        Assertions.assertTrue(merged == expectedCgResult)
    }

    @Test
    fun mergeMultiDisjoint() {
        val cgRes1 = CGResult(mapOf(b1Cg))
        val cgRes2 = CGResult(mapOf(b2Cg))
        val cgRes3 = CGResult(mapOf(b3Cg))
        val cgResults = listOf(cgRes1, cgRes2, cgRes3)
        val merged = cgResults.merge()
        Assertions.assertTrue(merged == expectedCgResult)
    }

    @Test
    fun mergeMultiOverlapping() {
        val cgRes1 = CGResult(mapOf(b1Cg, b2Cg))
        val cgRes2 = CGResult(mapOf(b2Cg, b3Cg))
        val cgRes3 = CGResult(mapOf(b1Cg, b3Cg))
        val cgResults = listOf(cgRes1, cgRes2, cgRes3)
        val merged = cgResults.merge()

        // this assertion does not work due to reordering of results, therefore we manually assert every element below
        // Assertions.assertTrue(merged == expectedCgResult)

        expectedCgResult.benchCalls.forEach { (b, calls) ->
            Assertions.assertTrue(merged.benchCalls.containsKey(b), "Merged CGResult does not contain benchmark $b")

            val mergedCalls = merged.benchCalls[b]
            Assertions.assertNotNull(mergedCalls)

            calls.forEach { c ->
                val cc = mergedCalls!!.contains(c)
                Assertions.assertTrue(cc, "Merged CGResult for bench ($b) does not contain MethodCall ($c)")
            }
        }
    }

    @Test
    fun reachable() {
        val cg = CGResult(mapOf(b1Cg))
        Assertions.assertTrue(cg.reachable(JarTestHelper.BenchParameterized.bench1, JarTestHelper.CoreA.m))
        Assertions.assertTrue(cg.reachable(JarTestHelper.BenchParameterized.bench1, JarTestHelper.CoreB.m))
    }

    @Test
    fun notReachable() {
        val cg = CGResult(mapOf(b1Cg))
        Assertions.assertFalse(cg.reachable(JarTestHelper.BenchParameterized.bench1, JarTestHelper.CoreD.m))
    }

    @Test
    fun multipleReachable() {
        val cg = CGResult(mapOf(b1Cg))
        Assertions.assertTrue(cg.anyReachable(JarTestHelper.BenchParameterized.bench1, listOf(JarTestHelper.CoreA.m, JarTestHelper.CoreB.m)))
    }

    @Test
    fun multipleNotReachable() {
        val cg = CGResult(mapOf(b2Cg))
        Assertions.assertFalse(cg.anyReachable(JarTestHelper.BenchParameterized.bench1, listOf(JarTestHelper.CoreA.m, JarTestHelper.CoreB.m)))
    }

    companion object {
        private val b1Cg = CGTestHelper.b1Cg
        private val b2Cg = CGTestHelper.b2Cg
        private val b3Cg = CGTestHelper.b3Cg
        private val expectedCgResult = CGResult(mapOf(b1Cg, b2Cg, b3Cg))
    }
}
