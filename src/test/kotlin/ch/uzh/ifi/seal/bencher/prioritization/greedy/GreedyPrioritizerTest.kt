package ch.uzh.ifi.seal.bencher.prioritization.greedy

import arrow.core.getOrHandle
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightTestHelper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import ch.uzh.ifi.seal.bencher.parameterizedBenchmarks
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import ch.uzh.ifi.seal.bencher.prioritization.Prioritizer
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizerTestHelper
import ch.uzh.ifi.seal.bencher.prioritization.PrioritySingle
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class GreedyPrioritizerTest {

    protected abstract fun prioritizer(cgRes: CGResult, methodWeights: MethodWeights, methodWeightMapper: MethodWeightMapper): Prioritizer

    private fun noPrios(param: Boolean) {
        val p = prioritizer(
                cgRes = PrioritizerTestHelper.cgFull,
                methodWeights = PrioritizerTestHelper.mwEmpty,
                methodWeightMapper = MethodWeightTestHelper.doubleMapper
        )

        val benchs = PrioritizerTestHelper.benchs.shuffled()
        val bs = p
            .prioritize(
                if (param) {
                    benchs.parameterizedBenchmarks()
                } else {
                    benchs
                }
            ).getOrHandle {
                Assertions.fail<String>("Could not retrieve prioritized benchs: $it")
                return
            }

        Assertions.assertEquals(bs.size, bs.size)

        bs.forEach { PrioritizerTestHelper.assertPriority(it, 1, bs.size, PrioritySingle(0.0)) }
    }

    @Test
    fun noPriosNonParam() = noPrios(false)

    @Test
    fun noPriosParam() = noPrios(true)

    private fun noCGResults(param: Boolean) {
        val p = prioritizer(
                cgRes = CGResult(mapOf()),
                methodWeights = PrioritizerTestHelper.mwFull,
                methodWeightMapper = MethodWeightTestHelper.doubleMapper
        )

        val benchs = PrioritizerTestHelper.benchs.shuffled()
        val bs = p
            .prioritize(
                if (param) {
                    benchs.parameterizedBenchmarks()
                } else {
                    benchs
                }
            ).getOrHandle {
                Assertions.fail<String>("Could not retrieve prioritized benchs: $it")
                return
            }

        Assertions.assertTrue(bs.isEmpty(), "Exepected 0 benchmarks in prioritized list, because no CGResult available")
    }

    @Test
    fun noCGResultsNonParam() = noCGResults(false)

    @Test
    fun noCGResultsParam() = noCGResults(true)

    private fun benchsNotInCG(param: Boolean) {
        val p = prioritizer(
                cgRes = PrioritizerTestHelper.cgTwo,
                methodWeights = PrioritizerTestHelper.mwFull,
                methodWeightMapper = MethodWeightTestHelper.doubleMapper
        )

        val benchs = PrioritizerTestHelper.benchs.shuffled()
        val bs = p
            .prioritize(
                if (param) {
                    benchs.parameterizedBenchmarks()
                } else {
                    benchs
                }
            )
            .getOrHandle {
                Assertions.fail<String>("Could not retrieve prioritized benchs: $it")
                return
            }

        assertionsBenchsNotInCG(param, bs, MethodWeightTestHelper.doubleFun)
    }

    @Test
    fun benchsNotInCGNonParam() = benchsNotInCG(false)

    @Test
    fun benchsNotInCGParam() = benchsNotInCG(true)

    protected abstract fun assertionsBenchsNotInCG(param: Boolean, bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double)


    /*
        weights:            A = 1, B = 2, C = 3, D = 4, E.mn1 = 5, E.mn2 = 6

                total           addtl

        b1      5.75 (A,B,C,E)  5.75 (A,B,C,E)

        b2      3 (C)           0

        b3      5 (B,C)         0

        b4      2.5 (A,D)       2 (D)
    */
    private fun withPrios(param: Boolean) {
        val p = prioritizer(
                cgRes = PrioritizerTestHelper.cgFull,
                methodWeights = PrioritizerTestHelper.mwFull,
                methodWeightMapper = MethodWeightTestHelper.doubleMapper
        )

        val benchs = PrioritizerTestHelper.benchs.shuffled()
        val bs = p
            .prioritize(
                if (param) {
                    benchs.parameterizedBenchmarks()
                } else {
                    benchs
                }
            )
            .getOrHandle {
                Assertions.fail<String>("Could not retrieve prioritized benchs: $it")
                return
            }

        assertionsWithPrios(param, bs, MethodWeightTestHelper.doubleFun)
    }

    @Test
    fun withPriosNonParam() = withPrios(false)

    @Test
    fun withPriosParam() = withPrios(true)

    protected abstract fun assertionsWithPrios(param: Boolean, bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double)


    /*
        weights:  A = 1, B = 1, C = 3, D = 10, E.mn1 = 4, E.mn2 = 5

                total           addtl

        b1      4.75 (A,B,C,E)  4.25 (B,C,E)

        b2      3 (C)           0

        b3      4 (B,C)         0

        b4      5.5 (A,D)       5.5 (A,D)
    */
    private fun withPriosDifferentWeights(param: Boolean) {
        val mw: MethodWeights = mapOf(
                Pair(JarTestHelper.CoreA.m, 1.0),
                Pair(JarTestHelper.CoreB.m, 1.0),
                Pair(JarTestHelper.CoreC.m, 3.0),
                Pair(JarTestHelper.CoreD.m, 10.0),
                Pair(JarTestHelper.CoreE.mn1_1, 4.0),
                Pair(JarTestHelper.CoreE.mn2, 5.0)
        )

        val p = prioritizer(
                cgRes = PrioritizerTestHelper.cgFull,
                methodWeights = mw,
                methodWeightMapper = MethodWeightTestHelper.doubleMapper
        )

        val benchs = PrioritizerTestHelper.benchs.shuffled()
        val bs = p
            .prioritize(
                if (param) {
                    benchs.parameterizedBenchmarks()
                } else {
                    benchs
                }
            )
            .getOrHandle {
                Assertions.fail<String>("Could not retrieve prioritized benchs: $it")
                return
            }

        assertionsWithPriosDifferentWeights(param, bs, MethodWeightTestHelper.doubleFun)
    }

    protected abstract fun assertionsWithPriosDifferentWeights(param: Boolean, bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double)

    @Test
    fun withPriosDifferentWeightsNonParam() = withPriosDifferentWeights(false)

    @Test
    fun withPriosDifferentWeightsParam() = withPriosDifferentWeights(true)
}
