package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeighter
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import org.junit.jupiter.api.Assertions
import java.nio.file.Path

class TotalPrioritizerTest : GreedyPrioritizerTest() {
    override fun prioritizer(cgRes: CGResult, methodWeights: MethodWeights): Prioritizer =
            TotalPrioritizer(cgResult = cgRes, methodWeights = methodWeights)

    override fun assertionsWithPrios(bs: List<PrioritizedMethod<Benchmark>>) {
        Assertions.assertTrue(bs.size == benchs.size)

        val b1 = bs[0]
        assertBenchmark(b1, JarTestHelper.BenchParameterized.bench1, 1, 4, 6.0)

        // benchmarks 2 and 3 are equal with respect to their ranking and therefore are in arbitrary order
        val b2 = bs[1]
        assertPriority(b2, 2, 4, 5.0)
        val b3 = bs[2]
        assertPriority(b3, 2, 4, 5.0)

        val b2b3 = (JarTestHelper.OtherBench.bench3 == b2.method  && JarTestHelper.BenchParameterized2.bench4 == b3.method) ||
                (JarTestHelper.OtherBench.bench3 == b3.method && JarTestHelper.BenchParameterized2.bench4 == b2.method)
        Assertions.assertTrue(b2b3, "Benchmark 3 or 4 not in output at rank 2")


        val b4 = bs[3]
        assertBenchmark(b4, JarTestHelper.BenchNonParameterized.bench2, 4, 4, 3.0)
    }

    override fun assertionsBenchsNotInCG(bs: List<PrioritizedMethod<Benchmark>>) {
        Assertions.assertTrue(bs.size == 2)

        val b1 = bs[0]
        assertBenchmark(b1, JarTestHelper.BenchParameterized.bench1, 1, 2, 6.0)

        val b2 = bs[1]
        assertBenchmark(b2, JarTestHelper.BenchNonParameterized.bench2, 2, 2, 3.0)
    }
}
