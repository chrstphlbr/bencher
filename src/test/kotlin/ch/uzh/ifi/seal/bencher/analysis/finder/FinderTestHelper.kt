package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import org.junit.jupiter.api.Assertions

object FinderTestHelper {
    fun contains(benchs: Iterable<Benchmark>, bench: Benchmark, jmhParamName: String = "", jmhParamVal: String = "") =
            benchs.find { b ->
                val p = if (jmhParamName.isBlank() && jmhParamVal.isBlank()) {
                    b.jmhParams.isEmpty()
                } else {
                    b.jmhParams.contains(Pair(jmhParamName, jmhParamVal))
                }
                b.clazz == bench.clazz && b.name == bench.name && p
            } != null



    fun assertParamTest(bs: Iterable<Benchmark>, bench: Benchmark) {
        val b1 = FinderTestHelper.contains(bs, bench, "str", "1")
        Assertions.assertTrue(b1)

        val b2 = FinderTestHelper.contains(bs, bench, "str", "2")
        Assertions.assertTrue(b2)

        val b3 = FinderTestHelper.contains(bs, bench, "str", "3")
        Assertions.assertTrue(b3)
    }

    fun print(benchs: Collection<Benchmark>) {
        benchs.forEach { b ->
            println(b)
        }
    }
}
