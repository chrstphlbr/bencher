package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JarBenchFinderTest {

    private fun contains(benchs: Collection<Benchmark>, bench: Benchmark, jmhParamName: String = "", jmhParamVal: String = "") =
            benchs.find { b ->
                val p = if (jmhParamName.isBlank() && jmhParamVal.isBlank()) {
                    b.jmhParams.isEmpty()
                } else {
                    b.jmhParams.contains(Pair(jmhParamName, jmhParamVal))
                }
                b.clazz == bench.clazz && b.name == bench.name && p
            } != null

    private fun print(benchs: Collection<Benchmark>) {
        benchs.forEach { b ->
            println(b)
        }
    }

    @Test
    fun twoBenchs121() {
        val url = JarHelper.jar2BenchsJmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.path)
        val benchs = f.all()
        if (benchs.isLeft()) {
            Assertions.fail<String>("Could not get benchmarks: ${benchs.left().get()}")
        }

        val bs = benchs.right().get()

        val b1 = contains(bs, bench2)
        Assertions.assertTrue(b1)

        assertParamTest(bs, bench1)
    }

    @Test
    fun fourBenchs121() {
        val url = JarHelper.jar4BenchsJmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.path)
        val benchs = f.all()
        if (benchs.isLeft()) {
            Assertions.fail<String>("Could not get benchmarks: ${benchs.left().get()}")
        }

        val bs = benchs.right().get()

        val b1 = contains(bs, bench2)
        Assertions.assertTrue(b1)

        assertParamTest(bs, bench1)

        val b5 = contains(bs, bench3)
        Assertions.assertTrue(b5)

        assertParamTest(bs, bench4)
    }

    fun assertParamTest(bs: List<Benchmark>, bench: Benchmark) {
        val b1 = contains(bs, bench, "str", "1")
        Assertions.assertTrue(b1)

        val b2 = contains(bs, bench, "str", "2")
        Assertions.assertTrue(b2)

        val b3 = contains(bs, bench, "str", "3")
        Assertions.assertTrue(b3)
    }

    @Test
    fun twoBenchs110() {
        val url = JarHelper.jar2BenchsJmh110.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.path)
        val benchs = f.all()
        if (benchs.isLeft()) {
            Assertions.fail<String>("Could not get benchmarks: ${benchs.left().get()}")
        }

        val bs = benchs.right().get()

        val b1 = contains(bs, bench2)
        Assertions.assertTrue(b1)

        val b2 = contains(bs, bench1)
        Assertions.assertTrue(b2)
    }

    @Test
    fun fourBenchs110() {
        val url = JarHelper.jar4BenchsJmh110.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.path)
        val benchs = f.all()
        if (benchs.isLeft()) {
            Assertions.fail<String>("Could not get benchmarks: ${benchs.left().get()}")
        }

        val bs = benchs.right().get()

        val b1 = contains(bs, bench2)
        Assertions.assertTrue(b1)

        val b2 = contains(bs, bench1)
        Assertions.assertTrue(b2)

        val b3 = contains(bs, bench3)
        Assertions.assertTrue(b3)

        val b4 = contains(bs, bench4)
        Assertions.assertTrue(b4)
    }

    companion object {
        val bench1 = JarHelper.BenchParameterized.bench1
        val bench2 = JarHelper.BenchNonParameterized.bench2
        val bench3 = JarHelper.OtherBench.bench3
        val bench4 = JarHelper.BenchParameterized2.bench4
    }
}
