package ch.uzh.ifi.seal.bencher.prioritization

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.fileResource
import ch.uzh.ifi.seal.bencher.parameterizedBenchmarks
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DefaultPrioritizerTest {

    private val javaSettings = JarTestHelper.javaSettings

    @Test
    fun empty() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()
        val p = DefaultPrioritizer(jar.toPath(), javaSettings)
        val pbs = p.prioritize(listOf()).getOrElse {
            Assertions.fail<String>("Could not prioritize benchmarks: $it")
            return
        }
        Assertions.assertEquals(0, pbs.size)
    }

    private fun defaultOrderAssertations(pbs: List<PrioritizedMethod<Benchmark>>, exp: List<Benchmark>) {
        Assertions.assertEquals(exp.size, pbs.size)

        (0 until exp.size).forEach { i ->
            val pb = pbs[i]
            Assertions.assertEquals(exp[i], pb.method)
            Assertions.assertEquals(Priority(rank = i + 1, total = exp.size, value = PrioritySingle((exp.size - i).toDouble())), pb.priority)
        }
    }

    private fun defaultOrder(param: Boolean) {
        val ibs = listOf(
            JarTestHelper.BenchParameterized.bench1,
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.OtherBench.bench3,
            JarTestHelper.BenchParameterized2v2.bench4
        )

        val iexp = listOf(
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.BenchParameterized.bench1,
            JarTestHelper.BenchParameterized2v2.bench4,
            JarTestHelper.OtherBench.bench3
        )

        val (bs, exp) = if (param) {
            Pair(ibs.parameterizedBenchmarks(), iexp.parameterizedBenchmarks())
        } else {
            Pair(ibs, iexp)
        }

        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()
        val p = DefaultPrioritizer(jar.toPath(), javaSettings)
        val pbs = p.prioritize(bs).getOrElse {
            Assertions.fail<String>("Could not prioritize benchmarks: $it")
            return
        }
        defaultOrderAssertations(pbs, exp)
    }

    @Test
    fun defaultOrderNonParam() {
        defaultOrder(false)
    }

    @Test
    fun defaultOrderParam() {
        defaultOrder(true)
    }

    private fun defaultOrderWithFuncParams(param: Boolean) {
        val benchWithFunParam =
            JarTestHelper.BenchParameterized.bench1.copy(params = listOf("org.openjdk.jmh.infra.Blackhole"))
        val ibs = listOf(
            benchWithFunParam,
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.OtherBench.bench3,
            JarTestHelper.BenchParameterized2v2.bench4
        )

        val iexp = listOf(
            JarTestHelper.BenchNonParameterized.bench2,
            benchWithFunParam,
            JarTestHelper.BenchParameterized2v2.bench4,
            JarTestHelper.OtherBench.bench3
        )

        val (bs, exp) = if (param) {
            Pair(ibs.parameterizedBenchmarks(), iexp.parameterizedBenchmarks())
        } else {
            Pair(ibs, iexp)
        }

        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()
        val p = DefaultPrioritizer(jar.toPath(), javaSettings)
        val pbs = p.prioritize(bs).getOrElse {
            Assertions.fail<String>("Could not prioritize benchmarks: $it")
            return
        }

        defaultOrderAssertations(pbs, exp)
    }

    @Test
    fun defaultOrderWithFuncParamsNonParam() {
        defaultOrderWithFuncParams(false)
    }

    @Test
    fun defaultOrderWithFuncParamsParam() {
        defaultOrderWithFuncParams(true)
    }
}
