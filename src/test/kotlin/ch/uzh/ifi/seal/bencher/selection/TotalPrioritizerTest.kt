package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import org.junit.jupiter.api.Assertions

class TotalPrioritizerTest : GreedyPrioritizerTest() {

    override fun prioritizer(cgRes: CGResult, methodWeights: MethodWeights, methodWeightMapper: MethodWeightMapper): Prioritizer =
            TotalPrioritizer(cgResult = cgRes, methodWeights = methodWeights, methodWeightMapper = methodWeightMapper)

    override fun assertionsWithPrios(bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        Assertions.assertTrue(bs.size == PrioritizerTestHelper.benchs.size)

        val b1 = bs[0]
        assertBenchmark(b1, JarTestHelper.BenchParameterized.bench1, 1, 4, mf(5.75))

        // benchmarks 2 and 3 are equal with respect to their ranking and therefore are in arbitrary order
        val b2 = bs[1]
        assertBenchmark(b2, JarTestHelper.OtherBench.bench3, 2, 4, mf(5.0))
        val b3 = bs[2]
        assertBenchmark(b3, JarTestHelper.BenchNonParameterized.bench2, 3, 4, mf(3.0))
        val b4 = bs[3]
        assertBenchmark(b4, JarTestHelper.BenchParameterized2.bench4, 4, 4, mf(2.5))
    }

    override fun assertionsBenchsNotInCG(bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        Assertions.assertTrue(bs.size == 2)

        val b1 = bs[0]
        assertBenchmark(b1, JarTestHelper.BenchParameterized.bench1, 1, 2, mf(5.75))

        val b2 = bs[1]
        assertBenchmark(b2, JarTestHelper.BenchNonParameterized.bench2, 2, 2, mf(3.0))
    }

    override fun assertionsWithPriosDifferentWeights(bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        Assertions.assertTrue(bs.size == PrioritizerTestHelper.benchs.size)

        val b1 = bs[0]
        assertBenchmark(b1, JarTestHelper.BenchParameterized2.bench4, 1, 4, mf(5.5))

        val b2 = bs[1]
        assertBenchmark(b2, JarTestHelper.BenchParameterized.bench1, 2, 4, mf(4.75))

        val b3 = bs[2]
        assertBenchmark(b3, JarTestHelper.OtherBench.bench3, 3, 4, mf(4.0))

        val b4 = bs[3]
        assertBenchmark(b4, JarTestHelper.BenchNonParameterized.bench2, 4, 4, mf(3.0))
    }
}
