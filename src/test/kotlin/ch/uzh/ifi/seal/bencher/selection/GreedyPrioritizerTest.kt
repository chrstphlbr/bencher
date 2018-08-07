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
                cgRes = cgFull,
                methodWeights = mwEmpty
        )

        val eBenchs = p.prioritize(benchs)

        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not retrieve prioritized benchs: ${eBenchs.left().get() }}")
        }

        val bs = eBenchs.right().get()
        Assertions.assertTrue(bs.size == benchs.size)

        bs.forEach { b ->
            assertPriority(b, 1, 4, 0.0)
        }
    }


    @Test
    fun noCGResults() {
        val p = prioritizer(
                cgRes = CGResult(mapOf()),
                methodWeights = mwFull
        )

        val eBenchs = p.prioritize(benchs)

        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not retrieve prioritized benchs: ${eBenchs.left().get() }}")
        }

        val benchs = eBenchs.right().get()
        Assertions.assertTrue(benchs.isEmpty(), "Exepected 0 benchmarks in prioritized list, because no CGResult available")
    }

    @Test
    fun withPrios() {
        val p = prioritizer(
                cgRes = cgFull,
                methodWeights = mwFull
        )

        val eBenchs = p.prioritize(benchs)

        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not retrieve prioritized benchs: ${eBenchs.left().get() }}")
        }

        assertionsWithPrios(eBenchs.right().get())
    }

    protected abstract fun assertionsWithPrios(bs: List<PrioritizedMethod<Benchmark>>)

    @Test
    fun benchsNotInCG() {
        val p = prioritizer(
                cgRes = cgTwo,
                methodWeights = mwFull
        )

        val eBenchs = p.prioritize(benchs)

        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not retrieve prioritized benchs: ${eBenchs.left().get() }}")
        }

        assertionsBenchsNotInCG(eBenchs.right().get())
    }

    protected abstract fun assertionsBenchsNotInCG(bs: List<PrioritizedMethod<Benchmark>>)


    companion object {
        val benchs = listOf(
                JarTestHelper.BenchParameterized.bench1,
                JarTestHelper.BenchNonParameterized.bench2,
                JarTestHelper.OtherBench.bench3,
                JarTestHelper.BenchParameterized2.bench4
        )

        val cgFull = CGResult(mapOf(CGTestHelper.b1Cg, CGTestHelper.b2Cg, CGTestHelper.b3Cg, CGTestHelper.b4Cg))
        val cgTwo = CGResult(mapOf(CGTestHelper.b1Cg, CGTestHelper.b2Cg))

        val mwFull: MethodWeights = mapOf(
                MethodWeightTestHelper.coreAmWeight,
                MethodWeightTestHelper.coreBmWeight,
                MethodWeightTestHelper.coreCmWeight,
                MethodWeightTestHelper.coreDmWeight
        )

        val mwEmpty: MethodWeights = mapOf()
    }
}
