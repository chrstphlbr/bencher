package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.finder.jdt.SourceCodeTestHelper
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.AbstractBenchmarkFinder
import org.junit.jupiter.api.Assertions

abstract class AbstractBenchJmhParamSourceTest {
    protected fun assertBenchStateObj(bf: AbstractBenchmarkFinder, benchs: List<Benchmark>) {
        val bench1 = benchs.filter { it.clazz == SourceCodeTestHelper.BenchsStateObj.fqn && it.name == SourceCodeTestHelper.BenchsStateObj.bench1.name }.first()
        assertSingleBenchmark(bf, bench1, bench1Source)
        val bench2 = benchs.filter { it.clazz == SourceCodeTestHelper.BenchsStateObj.fqn && it.name == SourceCodeTestHelper.BenchsStateObj.bench2.name }.first()
        assertSingleBenchmark(bf, bench2, bench2Source)
        val bench3 = benchs.filter { it.clazz == SourceCodeTestHelper.BenchsStateObj.fqn && it.name == SourceCodeTestHelper.BenchsStateObj.bench3.name }.first()
        assertSingleBenchmark(bf, bench3, bench3Source)
    }

    private fun assertSingleBenchmark(bf: AbstractBenchmarkFinder, bench: Benchmark, expectedResult: Map<String, String>) {
        val actual = bf.jmhParamSource(bench)

        if (actual != expectedResult) {
            Assertions.fail<String>("Invalid jmh param source: actual => $actual, expected => $expectedResult")
        }

        Assertions.assertEquals(expectedResult.size, actual.size)
    }

    companion object {
        @JvmStatic
        protected val bench1Source = mutableMapOf(
                "str1" to SourceCodeTestHelper.ObjectA.fqn,
                "str2" to SourceCodeTestHelper.ObjectA.fqn,
                "str4" to SourceCodeTestHelper.BenchsStateObj.fqn
        )

        @JvmStatic
        protected val bench2Source = mutableMapOf(
                "str1" to SourceCodeTestHelper.ObjectA.fqn,
                "str2" to SourceCodeTestHelper.ObjectA.fqn,
                "str3" to SourceCodeTestHelper.ObjectB.fqn,
                "str4" to SourceCodeTestHelper.BenchsStateObj.fqn
        )

        @JvmStatic
        protected val bench3Source = mutableMapOf(
                "str1" to SourceCodeTestHelper.ObjectB.fqn,
                "str2" to SourceCodeTestHelper.ObjectA.fqn,
                "str3" to SourceCodeTestHelper.ObjectB.fqn,
                "str4" to SourceCodeTestHelper.BenchsStateObj.fqn
        )
    }
}