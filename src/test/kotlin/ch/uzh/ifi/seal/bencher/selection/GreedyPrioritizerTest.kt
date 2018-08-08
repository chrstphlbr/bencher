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

    protected fun assertPriority(b: PrioritizedMethod<out Method>, rank: Int, total: Int, value: Double) {
        val r = b.priority.rank
        Assertions.assertTrue(r == rank, "Benchmark does not have priority rank $rank (was $r")
        val t = b.priority.total
        Assertions.assertTrue(t == total, "Benchmark does not have priority total $total (was $t")
        val v = b.priority.value
        Assertions.assertTrue(v == value, "Benchmark does not have priority value $value (was $v)")
    }

    protected fun assertBenchmark(b: PrioritizedMethod<Benchmark>, expectedBench: Benchmark, rank: Int, total: Int, value: Double) {
        Assertions.assertTrue(b.method == expectedBench, "Benchmark not as expected: was ${b.method}, expected $expectedBench")
        assertPriority(b, rank, total, value)
    }

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
        weights:            A = 1, B = 2, C = 3, D = 4

                total           addtl

        b1      6 (A,B,C)       6 (A,B,C)

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
        weights:  A = 1, B = 1, C = 3, D = 5

                total           addtl

        b1      5 (A,B,C)       4 (B,C) || 0

        b2      3 (C)           0

        b3      4 (B,C)         4 (B,C) || 0

        b4      6 (A,D)         6 (A,D)
    */
    @Test
    fun withPriosDifferentWeights() {
        val mw: MethodWeights = mapOf(
                Pair(JarTestHelper.CoreA.m, 1.0),
                Pair(JarTestHelper.CoreB.m, 1.0),
                Pair(JarTestHelper.CoreC.m, 3.0),
                Pair(JarTestHelper.CoreD.m, 5.0)
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
}
