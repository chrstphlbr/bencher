package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions

abstract class AbstractAsmBenchFinderTest {
    fun assertTwoBenchs(bs: Iterable<Benchmark>) {
        val b1 = FinderTestHelper.contains(bs, bench2)
        Assertions.assertTrue(b1)

        FinderTestHelper.assertParamTest(bs, bench1)
    }

    fun assertFourBenchs(bs: Iterable<Benchmark>) {
        val b1 = FinderTestHelper.contains(bs, bench2)
        Assertions.assertTrue(b1)

        FinderTestHelper.assertParamTest(bs, bench1)

        val b5 = FinderTestHelper.contains(bs, bench3)
        Assertions.assertTrue(b5)

        FinderTestHelper.assertParamTest(bs, bench4)
    }

    companion object {
        val bench1 = JarTestHelper.BenchParameterized.bench1
        val bench2 = JarTestHelper.BenchNonParameterized.bench2
        val bench3 = JarTestHelper.OtherBench.bench3
        val bench4 = JarTestHelper.BenchParameterized2.bench4
    }
}
