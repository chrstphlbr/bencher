package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.analysis.JarHelper
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

    companion object {
        val b1Cg = Pair(
                JarHelper.BenchParameterized.bench1,
                listOf(
                        MethodCall(JarHelper.CoreA.m, 1),
                        MethodCall(JarHelper.CoreB.m, 1),
                        MethodCall(JarHelper.CoreC.m, 2),
                        MethodCall(JarHelper.CoreA.m, 2),
                        MethodCall(JarHelper.CoreB.m, 2)
                )
        )

        val b2Cg = Pair(
                JarHelper.BenchNonParameterized.bench2,
                listOf(
                        MethodCall(JarHelper.CoreC.m, 1)
                )
        )

        val b3Cg = Pair(
                JarHelper.OtherBench.bench3,
                listOf(
                        MethodCall(JarHelper.CoreB.m, 1),
                        MethodCall(JarHelper.CoreC.m, 2)
                )
        )

        val expectedCgResult = CGResult(mapOf(b1Cg, b2Cg, b3Cg))
    }
}
