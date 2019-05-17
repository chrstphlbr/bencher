package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.IdentityMethodWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import org.junit.jupiter.api.Assertions

class AdditionalPrioritizerTest : GreedyPrioritizerTest() {
    override fun prioritizer(cgRes: CGResult, methodWeights: MethodWeights, methodWeightMapper: MethodWeightMapper): Prioritizer =
            AdditionalPrioritizer(cgResult = cgRes, methodWeights = methodWeights, methodWeightMapper = methodWeightMapper)

    override fun assertionsWithPrios(bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        Assertions.assertTrue(bs.size == PrioritizerTestHelper.benchs.size)

        val b1 = bs[0]

        assertBenchmark(b1, JarTestHelper.BenchParameterized.bench1, 1, 4, mf(5.75))

        val b2 = bs[1]
        assertBenchmark(b2, JarTestHelper.BenchParameterized2.bench4, 2, 4, mf(2.0))
        
        // benchmarks 2 and 3 are equal with respect to their ranking and therefore are in arbitrary order
        val b3 = bs[2]
        assertPriority(b3, 3, 4, mf(0.0))
        val b4 = bs[3]
        assertPriority(b4, 3, 4, mf(0.0))

        val b3b4 = (JarTestHelper.OtherBench.bench3 == b3.method  && JarTestHelper.BenchNonParameterized.bench2 == b4.method) ||
                (JarTestHelper.OtherBench.bench3 == b4.method && JarTestHelper.BenchNonParameterized.bench2 == b3.method)
        Assertions.assertTrue(b3b4, "Benchmark 3 or 4 not in output at rank 3")
    }

    override fun assertionsBenchsNotInCG(bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        Assertions.assertTrue(bs.size == 2)

        val b1 = bs[0]
        assertBenchmark(b1, JarTestHelper.BenchParameterized.bench1, 1, 2, mf(5.75))

        val b2 = bs[1]
        assertBenchmark(b2, JarTestHelper.BenchNonParameterized.bench2, 2, 2, mf(0.0))
    }


    override fun assertionsWithPriosDifferentWeights(bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        Assertions.assertTrue(bs.size == PrioritizerTestHelper.benchs.size)

        val b1 = bs[0]
        assertBenchmark(b1, JarTestHelper.BenchParameterized2.bench4, 1, 4, mf(5.5))

        val b2 = bs[1]
        assertBenchmark(b2, JarTestHelper.BenchParameterized.bench1, 2, 4, mf(4.25))

        // benchmarks 3 and 4 are equal with respect to their ranking and therefore are in arbitrary order
        val b3 = bs[2]
        assertPriority(b3, 3, 4, mf(0.0))
        val b4 = bs[3]
        assertPriority(b4, 3, 4, mf(0.0))

        val b2b3 = (JarTestHelper.BenchNonParameterized.bench2 == b3.method  && JarTestHelper.OtherBench.bench3 == b4.method) ||
                (JarTestHelper.BenchNonParameterized.bench2 == b4.method && JarTestHelper.OtherBench.bench3 == b3.method)
        Assertions.assertTrue(b2b3, "Benchmark 3 or 4 not in output at rank 3")
    }
}
