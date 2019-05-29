package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import ch.uzh.ifi.seal.bencher.parameterizedBenchmarks
import org.junit.jupiter.api.Assertions

class AdditionalPrioritizerTest : GreedyPrioritizerTest() {

    private val eb1 = JarTestHelper.BenchParameterized.bench1
    private val eb2 = JarTestHelper.BenchNonParameterized.bench2
    private val eb3 = JarTestHelper.OtherBench.bench3
    private val eb4 = JarTestHelper.BenchParameterized2.bench4

    private val ebs1 = eb1.parameterizedBenchmarks()
    private val eb11 = ebs1[0]
    private val eb12 = ebs1[1]
    private val eb13 = ebs1[2]
    private val ebs4 = eb4.parameterizedBenchmarks()
    private val eb41 = ebs4[0]
    private val eb42 = ebs4[1]
    private val eb43 = ebs4[2]


    override fun prioritizer(cgRes: CGResult, methodWeights: MethodWeights, methodWeightMapper: MethodWeightMapper): Prioritizer =
            AdditionalPrioritizer(cgResult = cgRes, methodWeights = methodWeights, methodWeightMapper = methodWeightMapper)

    private fun assertionsBenchsNotInCGNonParam(bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        PrioritizerTestHelper.assertBenchmarks(
                eBenchmarks = listOf(
                        PrioritizerTestHelper.ExpectedPrioBench(
                                benchmark = eb1,
                                rank = 1,
                                value = mf(5.75)
                        ),
                        PrioritizerTestHelper.ExpectedPrioBench(
                                benchmark = eb2,
                                rank = 2,
                                value = mf(0.0)
                        )
                ),
                pBenchmarks = bs
        )
    }

    private fun assertionsBenchsNotInCGParam(bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        val eSize = 4
        Assertions.assertEquals(eSize, bs.size)

        PrioritizerTestHelper.assertBenchmark(bs[0], eb11, 1, eSize, mf(5.75))

        PrioritizerTestHelper.assertEqualRankBenchmarks(
                eBenchmarks = listOf(eb12, eb13, eb2),
                pBenchmarks = bs.subList(1, bs.size),
                rank = 2,
                total = eSize,
                value = mf(0.0)
        )
    }

    override fun assertionsBenchsNotInCG(param: Boolean, bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) =
            if (param) {
                assertionsBenchsNotInCGParam(bs, mf)
            } else {
                assertionsBenchsNotInCGNonParam(bs, mf)
            }

    private fun assertionsWithPriosNonParam(bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        val eSize = PrioritizerTestHelper.benchs.size
        Assertions.assertEquals(eSize, bs.size)

        PrioritizerTestHelper.assertBenchmarks(
                eBenchmarks = listOf(
                        PrioritizerTestHelper.ExpectedPrioBench(
                                benchmark = eb1,
                                rank = 1,
                                value = mf(5.75)
                        ),
                        PrioritizerTestHelper.ExpectedPrioBench(
                                benchmark = eb4,
                                rank = 2,
                                value = mf(2.0)
                        )
                ),
                pBenchmarks = bs.subList(0, 2),
                total = eSize
        )

        PrioritizerTestHelper.assertEqualRankBenchmarks(
                eBenchmarks = listOf(eb2, eb3),
                pBenchmarks = bs.subList(2, bs.size),
                rank = 3,
                total = eSize,
                value = mf(0.0)
        )
    }

    private fun assertionsWithPriosParam(bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        val eSize = PrioritizerTestHelper.benchs.parameterizedBenchmarks().size
        Assertions.assertEquals(eSize, bs.size)

        PrioritizerTestHelper.assertBenchmarks(
                eBenchmarks = listOf(
                        PrioritizerTestHelper.ExpectedPrioBench(
                                benchmark = eb11,
                                rank = 1,
                                value = mf(5.75)
                        ),
                        PrioritizerTestHelper.ExpectedPrioBench(
                                benchmark = eb41,
                                rank = 2,
                                value = mf(2.0)
                        )
                ),
                pBenchmarks = bs.subList(0, 2),
                total = eSize
        )

        PrioritizerTestHelper.assertEqualRankBenchmarks(
                eBenchmarks = listOf(eb12, eb13, eb2, eb3, eb42, eb43),
                pBenchmarks = bs.subList(2, bs.size),
                rank = 3,
                total = eSize,
                value = mf(0.0)
        )
    }

    override fun assertionsWithPrios(param: Boolean, bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) =
            if (param) {
                assertionsWithPriosParam(bs, mf)
            } else {
                assertionsWithPriosNonParam(bs, mf)
            }

    private fun assertionsWithPriosDifferentWeightsNonParam(bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        val eSize = PrioritizerTestHelper.benchs.size
        Assertions.assertEquals(eSize, bs.size)

        PrioritizerTestHelper.assertBenchmarks(
                eBenchmarks = listOf(
                        PrioritizerTestHelper.ExpectedPrioBench(
                                benchmark = eb4,
                                rank = 1,
                                value = mf(5.5)
                        ),
                        PrioritizerTestHelper.ExpectedPrioBench(
                                benchmark = eb1,
                                rank = 2,
                                value = mf(4.25)
                        )
                ),
                pBenchmarks = bs.subList(0, 2),
                total = eSize
        )

        PrioritizerTestHelper.assertEqualRankBenchmarks(
                eBenchmarks = listOf(eb2, eb3),
                pBenchmarks = bs.subList(2, bs.size),
                rank = 3,
                total = eSize,
                value = mf(0.0)
        )
    }

    private fun assertionsWithPriosDifferentWeightsParam(bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        val eSize = PrioritizerTestHelper.benchs.parameterizedBenchmarks().size
        Assertions.assertEquals(eSize, bs.size)

        PrioritizerTestHelper.assertBenchmarks(
                eBenchmarks = listOf(
                        PrioritizerTestHelper.ExpectedPrioBench(
                                benchmark = eb41,
                                rank = 1,
                                value = mf(5.5)
                        ),
                        PrioritizerTestHelper.ExpectedPrioBench(
                                benchmark = eb11,
                                rank = 2,
                                value = mf(4.25)
                        )
                ),
                pBenchmarks = bs.subList(0, 2),
                total = eSize
        )

        PrioritizerTestHelper.assertEqualRankBenchmarks(
                eBenchmarks = listOf(eb12, eb13, eb2, eb3, eb42, eb43),
                pBenchmarks = bs.subList(2, bs.size),
                rank = 3,
                total = eSize,
                value = mf(0.0)
        )
    }

    override fun assertionsWithPriosDifferentWeights(param: Boolean, bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) =
            if (param) {
                assertionsWithPriosDifferentWeightsParam(bs, mf)
            } else {
                assertionsWithPriosDifferentWeightsNonParam(bs, mf)
            }
}
