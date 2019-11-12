package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import ch.uzh.ifi.seal.bencher.analysis.finder.FinderTestHelper
import org.junit.jupiter.api.Assertions

typealias jmhBenchs = List<Triple<Set<Benchmark>, Set<SetupMethod>, Set<TearDownMethod>>>

abstract class AbstractJdtBenchFinderTest {
    fun assertTwoBenchs(bs: Iterable<Benchmark>) {
        val b1 = FinderTestHelper.contains(bs, bench2.bench2)
        Assertions.assertTrue(b1)

        FinderTestHelper.assertParamTest(bs, bench1.bench1)
    }

    fun assertBenchsSetupsTearDowns(jmhBenchs: jmhBenchs) {
        jmhBenchs.forEach { (bs, sus, tds) ->
            bs.forEach { b ->
                when (b) {
                    bench1.bench1 ->
                        Assertions.assertTrue(sus.contains(bench1.setup) && sus.size == 1 && tds.isEmpty(),
                                "$b invalid setups/teardowns")
                    bench2.bench2 ->
                        Assertions.assertTrue(sus.isEmpty() && tds.isEmpty(),
                                "$b invalid setups/teardowns")
                    bench3.bench3 ->
                        Assertions.assertTrue(sus.size == 1 && tds.size == 1 && sus.contains(bench3.setup) && tds.contains(bench3.tearDown),
                                "$b invalid setups/teardowns")
                    bench4.bench4 ->
                        Assertions.assertTrue(sus.contains(bench4.setup) && sus.size == 1 && tds.isEmpty(),
                                "$b invalid setups/teardowns")
                }
            }
        }
    }

    companion object {
        val bench1 = SourceCodeTestHelper.BenchParameterized
        val bench2 = SourceCodeTestHelper.BenchNonParameterized
        val bench3 = SourceCodeTestHelper.OtherBench
        val bench4 = SourceCodeTestHelper.BenchParameterized2
    }
}
