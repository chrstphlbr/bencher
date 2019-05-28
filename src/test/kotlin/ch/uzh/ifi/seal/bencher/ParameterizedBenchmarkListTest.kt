package ch.uzh.ifi.seal.bencher

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ParameterizedBenchmarkListTest {
    @Test
    fun empty() {
        val bs = listOf<Benchmark>()
        val pbs = bs.parameterizedBenchmarks()
        Assertions.assertEquals(0, pbs.size)
    }

    @Test
    fun nonParamBenchs() {
        val bs = listOf(
                JarTestHelper.BenchNonParameterized.bench2,
                JarTestHelper.OtherBench.bench3
        )
        val pbs = bs.parameterizedBenchmarks()
        Assertions.assertEquals(bs, pbs)
    }

    @Test
    fun oneParamBench() {
        val bs = listOf(
                JarTestHelper.BenchNonParameterized.bench2,
                JarTestHelper.BenchParameterized2.bench4
        )
        val pbs = bs.parameterizedBenchmarks()
        val ebs = bs.flatMap { it.parameterizedBenchmarks() }
        Assertions.assertEquals(ebs, pbs)
    }
}
