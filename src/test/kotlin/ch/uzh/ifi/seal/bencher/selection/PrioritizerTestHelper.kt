package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGTestHelper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightTestHelper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import org.junit.jupiter.api.Assertions

object PrioritizerTestHelper {

    val benchs = listOf(
            JarTestHelper.BenchParameterized.bench1,
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.OtherBench.bench3,
            JarTestHelper.BenchParameterized2.bench4
    )

    val benchsPrio = benchs.flatMap { it.parameterizedBenchmarks() }

    val cgFull = CGResult(mapOf(CGTestHelper.b1Cg, CGTestHelper.b2Cg, CGTestHelper.b3Cg, CGTestHelper.b4Cg))
    val cgTwo = CGResult(mapOf(CGTestHelper.b1Cg, CGTestHelper.b2Cg))

    val mwFull: MethodWeights = mapOf(
            MethodWeightTestHelper.coreAmWeight,
            MethodWeightTestHelper.coreBmWeight,
            MethodWeightTestHelper.coreCmWeight,
            MethodWeightTestHelper.coreDmWeight,
            MethodWeightTestHelper.coreEmn1Weight,
            MethodWeightTestHelper.coreEmn2Weight

    )

    val mwEmpty: MethodWeights = mapOf()

    fun assertPriority(b: PrioritizedMethod<out Method>, rank: Int, total: Int, value: Double) {
        val r = b.priority.rank
        Assertions.assertTrue(r == rank, "${b.method} does not have priority rank $rank (was $r")
        val t = b.priority.total
        Assertions.assertTrue(t == total, "${b.method} does not have priority total $total (was $t")
        val v = b.priority.value
        Assertions.assertTrue(v == value, "${b.method} does not have priority value $value (was $v)")
    }

    fun assertBenchmark(b: PrioritizedMethod<Benchmark>, expectedBench: Benchmark, rank: Int, total: Int, value: Double) {
        Assertions.assertTrue(b.method == expectedBench, "Benchmark not as expected: was ${b.method}, expected $expectedBench")
        assertPriority(b, rank, total, value)
    }
}
