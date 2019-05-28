package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.fileResource
import ch.uzh.ifi.seal.bencher.parameterizedBenchmarks
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DefaultPrioritizerTest {
    @Test
    fun empty() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()
        val p = DefaultPrioritizer(jar.toPath())
        val epbs = p.prioritize(listOf())
        if (epbs.isLeft()) {
            Assertions.fail<String>("Could not prioritize benchmarks: ${epbs.left().get()}")
        }
        val pbs = epbs.right().get()
        Assertions.assertEquals(0, pbs.size)
    }

    private fun defaultOrderAssertations(pbs: List<PrioritizedMethod<Benchmark>>, exp: List<Benchmark>) {
        Assertions.assertEquals(exp.size, pbs.size)

        (0 until exp.size).forEach { i ->
            val pb = pbs[i]
            Assertions.assertEquals(exp[i], pb.method)
            Assertions.assertEquals(Priority(rank = i+1, total = exp.size, value = (exp.size-i).toDouble()), pb.priority)
        }
    }

    @Test
    fun defaultOrder() {
        val benchs = listOf(
                JarTestHelper.BenchParameterized.bench1,
                JarTestHelper.BenchNonParameterized.bench2,
                JarTestHelper.OtherBench.bench3,
                JarTestHelper.BenchParameterized2v2.bench4
        )


        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()
        val p = DefaultPrioritizer(jar.toPath())
        val epbs = p.prioritize(benchs)
        if (epbs.isLeft()) {
            Assertions.fail<String>("Could not prioritize benchmarks: ${epbs.left().get()}")
        }
        val pbs = epbs.right().get()

        val exp = listOf(
                JarTestHelper.BenchNonParameterized.bench2,
                JarTestHelper.BenchParameterized.bench1,
                JarTestHelper.BenchParameterized2v2.bench4,
                JarTestHelper.OtherBench.bench3
        )
        defaultOrderAssertations(pbs, exp)
    }

    @Test
    fun defaultOrderParameterized() {
        val benchs = listOf(
                JarTestHelper.BenchParameterized.bench1,
                JarTestHelper.BenchNonParameterized.bench2,
                JarTestHelper.OtherBench.bench3,
                JarTestHelper.BenchParameterized2v2.bench4
        ).parameterizedBenchmarks()


        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()
        val p = DefaultPrioritizer(jar.toPath())
        val epbs = p.prioritize(benchs)
        if (epbs.isLeft()) {
            Assertions.fail<String>("Could not prioritize benchmarks: ${epbs.left().get()}")
        }
        val pbs = epbs.right().get()

        pbs.forEach { println(it) }

        val exp = listOf(
                JarTestHelper.BenchNonParameterized.bench2,
                JarTestHelper.BenchParameterized.bench1,
                JarTestHelper.BenchParameterized2v2.bench4,
                JarTestHelper.OtherBench.bench3
        ).parameterizedBenchmarks()

        defaultOrderAssertations(pbs, exp)
    }

    @Test
    fun defaultOrderWithFuncParams() {
        val benchWithFunfParam = JarTestHelper.BenchParameterized.bench1.copy(params = listOf("org.openjdk.jmh.infra.Blackhole"))
        val benchs = listOf(
                benchWithFunfParam,
                JarTestHelper.BenchNonParameterized.bench2,
                JarTestHelper.OtherBench.bench3,
                JarTestHelper.BenchParameterized2v2.bench4
        )


        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()
        val p = DefaultPrioritizer(jar.toPath())
        val epbs = p.prioritize(benchs)
        if (epbs.isLeft()) {
            Assertions.fail<String>("Could not prioritize benchmarks: ${epbs.left().get()}")
        }
        val pbs = epbs.right().get()

        val exp = listOf(
                JarTestHelper.BenchNonParameterized.bench2,
                benchWithFunfParam,
                JarTestHelper.BenchParameterized2v2.bench4,
                JarTestHelper.OtherBench.bench3
        )
        defaultOrderAssertations(pbs, exp)
    }
}
