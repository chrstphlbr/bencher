package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.fileResource
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

    @Test
    fun defaultOrder() {
        val benchs = listOf(
                JarTestHelper.BenchParameterized.bench1,
                JarTestHelper.BenchNonParameterized.bench2,
                JarTestHelper.OtherBench.bench3,
                JarTestHelper.BenchParameterized2.bench4
        )


        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()
        val p = DefaultPrioritizer(jar.toPath())
        val epbs = p.prioritize(benchs)
        if (epbs.isLeft()) {
            Assertions.fail<String>("Could not prioritize benchmarks: ${epbs.left().get()}")
        }
        val pbs = epbs.right().get()
        Assertions.assertEquals(4, pbs.size)

        val pb1 = pbs[0]
        Assertions.assertEquals(JarTestHelper.BenchNonParameterized.bench2, pb1.method)
        Assertions.assertEquals(Priority(rank = 1, total = 4, value = 4.0), pb1.priority)

        val pb2 = pbs[1]
        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, pb2.method)
        Assertions.assertEquals(Priority(rank = 2, total = 4, value = 3.0), pb2.priority)

        val pb3 = pbs[2]
        Assertions.assertEquals(JarTestHelper.BenchParameterized2.bench4, pb3.method)
        Assertions.assertEquals(Priority(rank = 3, total = 4, value = 2.0), pb3.priority)

        val pb4 = pbs[3]
        Assertions.assertEquals(JarTestHelper.OtherBench.bench3, pb4.method)
        Assertions.assertEquals(Priority(rank = 4, total = 4, value = 1.0), pb4.priority)
    }
}
