package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGTestHelper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightTestHelper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class GreedyPrioritizerTest {

    protected abstract fun prioritizer(cgRes: CGResult, methodWeights: MethodWeights): Prioritizer

    @Test
    fun noPrios() {
        val p = prioritizer(
                cgRes = PrioritizerTestHelper.cgFull,
                methodWeights = PrioritizerTestHelper.mwEmpty
        )

        val eBenchs = p.prioritize(PrioritizerTestHelper.benchs)

        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not retrieve prioritized benchs: ${eBenchs.left().get() }}")
        }

        val bs = eBenchs.right().get()
        Assertions.assertTrue(bs.size == PrioritizerTestHelper.benchs.size)

        bs.forEach { b ->
            assertPriority(b, 1, 4, 0.0)
        }
    }


    @Test
    fun noCGResults() {
        val p = prioritizer(
                cgRes = CGResult(mapOf()),
                methodWeights = PrioritizerTestHelper.mwFull
        )

        val eBenchs = p.prioritize(PrioritizerTestHelper.benchs.shuffled())

        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not retrieve prioritized benchs: ${eBenchs.left().get() }}")
        }

        val benchs = eBenchs.right().get()
        Assertions.assertTrue(benchs.isEmpty(), "Exepected 0 benchmarks in prioritized list, because no CGResult available")
    }

    @Test
    fun benchsNotInCG() {
        val p = prioritizer(
                cgRes = PrioritizerTestHelper.cgTwo,
                methodWeights = PrioritizerTestHelper.mwFull
        )

        val eBenchs = p.prioritize(PrioritizerTestHelper.benchs.shuffled())

        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not retrieve prioritized benchs: ${eBenchs.left().get() }}")
        }

        assertionsBenchsNotInCG(eBenchs.right().get())
    }

    protected abstract fun assertionsBenchsNotInCG(bs: List<PrioritizedMethod<Benchmark>>)


    /*
        weights:            A = 1, B = 2, C = 3, D = 4, E.mn1 = 5, E.mn2 = 6

                total           addtl

        b1      11.5 (A,B,C,E)  11.5 (A,B,C,E)

        b2      3 (C)           0

        b3      5 (B,C)         0

        b4      5 (A,D)         4 (D)
    */
    @Test
    fun withPrios() {
        val p = prioritizer(
                cgRes = PrioritizerTestHelper.cgFull,
                methodWeights = PrioritizerTestHelper.mwFull
        )

        val eBenchs = p.prioritize(PrioritizerTestHelper.benchs.shuffled())

        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not retrieve prioritized benchs: ${eBenchs.left().get() }}")
        }

        assertionsWithPrios(eBenchs.right().get())
    }

    protected abstract fun assertionsWithPrios(bs: List<PrioritizedMethod<Benchmark>>)

    /*
        weights:  A = 1, B = 1, C = 3, D = 10, E.mn1 = 4, E.mn2 = 5

                total           addtl

        b1      9.5 (A,B,C,E)   8.5 (B,C,E)

        b2      3 (C)           0

        b3      4 (B,C)         0

        b4      11 (A,D)        11 (A,D)
    */
    @Test
    fun withPriosDifferentWeights() {
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
                methodWeights = mw
        )

        val eBenchs = p.prioritize(PrioritizerTestHelper.benchs.shuffled())

        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not retrieve prioritized benchs: ${eBenchs.left().get() }}")
        }

        val bs = eBenchs.right().get()

        assertionsWithPriosDifferentWeights(bs)
    }

    protected abstract fun assertionsWithPriosDifferentWeights(bs: List<PrioritizedMethod<Benchmark>>)

    companion object {
        @JvmStatic
        protected val assertPriority = PrioritizerTestHelper::assertPriority
        @JvmStatic
        protected val assertBenchmark = PrioritizerTestHelper::assertBenchmark
    }
}
