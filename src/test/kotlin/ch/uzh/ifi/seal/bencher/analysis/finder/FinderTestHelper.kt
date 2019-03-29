package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import org.junit.jupiter.api.Assertions

object FinderTestHelper {
    fun contains(benchs: Iterable<Benchmark>, bench: Benchmark, jmhParams: List<Pair<String, String>> = listOf()) =
            benchs.find { b ->
                val p = if (jmhParams.isEmpty()) {
                    b.jmhParams.isEmpty()
                } else {
                    b.jmhParams == jmhParams
                }
                b.clazz == bench.clazz && b.name == bench.name && p
            } != null



    fun assertParamTest(bs: Iterable<Benchmark>, bench: Benchmark, twoJmhParams: Boolean = false) {
        val jmhParams = listOf(Pair("str", "1"), Pair("str", "2"), Pair("str", "3"))
        val p = if (twoJmhParams) {
            jmhParams + listOf(Pair("str2", "1"), Pair("str2", "2"), Pair("str2", "3"))
        } else {
            jmhParams
        }

        val b = FinderTestHelper.contains(bs, bench, p)
        Assertions.assertTrue(b)
    }

    fun print(benchs: Collection<Benchmark>) {
        benchs.forEach { b ->
            println(b)
        }
    }
}
