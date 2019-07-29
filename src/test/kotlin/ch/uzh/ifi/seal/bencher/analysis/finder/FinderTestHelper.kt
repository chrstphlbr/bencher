package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import org.junit.jupiter.api.Assertions

object FinderTestHelper {
    fun contains(benchs: Iterable<Benchmark>, bench: Benchmark, withJmhParams: Boolean = true): Boolean =
            benchs.find { b ->
                if (withJmhParams) {
                    b == bench
                } else {
                    b == bench.copy(jmhParams = listOf())
                }
            } != null

    fun assertParamTest(bs: Iterable<Benchmark>, bench: Benchmark, withJmhParams: Boolean = true) {
        val b = FinderTestHelper.contains(bs, bench, withJmhParams)
        Assertions.assertTrue(b)
    }

    fun print(benchs: Collection<Benchmark>) {
        benchs.forEach { b ->
            println(b)
        }
    }
}
