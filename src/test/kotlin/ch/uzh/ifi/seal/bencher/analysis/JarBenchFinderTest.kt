package ch.uzh.ifi.seal.bencher.analysis

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JarBenchFinderTest {

    private fun contains(benchs: Collection<Benchmark>, clazz: String, method: String, jmhParamName: String = "", jmhParamVal: String = "") =
            benchs.find { b ->
                val p = if (jmhParamName.isBlank() && jmhParamVal.isBlank()) {
                    b.jmhParams.isEmpty()
                } else {
                    b.jmhParams.contains(Pair(jmhParamName, jmhParamVal))
                }
                b.clazz == clazz && b.name == method && p
            } != null

    private fun print(benchs: Collection<Benchmark>) {
        benchs.forEach { b ->
            println(b)
        }
    }

    @Test
    fun twoBenchs121() {
        val url = this::class.java.classLoader.getResource(JarHelper.jar2BenchsJmh121)
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.path)
        val benchs = f.all()
        if (benchs.isLeft()) {
            Assertions.fail<String>("Could not get benchmarks: ${benchs.left().get()}")
        }

        val bs = benchs.right().get()

        val b1 = contains(bs, "org.sample.BenchNonParameterized", "bench2")
        Assertions.assertTrue(b1)

        val b2 = contains(bs, "org.sample.BenchParameterized","bench1", "str", "1")
        Assertions.assertTrue(b2)

        val b3 = contains(bs, "org.sample.BenchParameterized","bench1", "str", "2")
        Assertions.assertTrue(b3)

        val b4 = contains(bs, "org.sample.BenchParameterized","bench1", "str", "3")
        Assertions.assertTrue(b4)
    }

    @Test
    fun threeBenchs121() {
        val url = this::class.java.classLoader.getResource(JarHelper.jar3BenchsJmh121)
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.file)
        val benchs = f.all()
        if (benchs.isLeft()) {
            Assertions.fail<String>("Could not get benchmarks: ${benchs.left().get()}")
        }

        val bs = benchs.right().get()

        val b1 = contains(bs, "org.sample.BenchNonParameterized", "bench2")
        Assertions.assertTrue(b1)

        val b2 = contains(bs, "org.sample.BenchParameterized","bench1", "str", "1")
        Assertions.assertTrue(b2)

        val b3 = contains(bs, "org.sample.BenchParameterized","bench1", "str", "2")
        Assertions.assertTrue(b3)

        val b4 = contains(bs, "org.sample.BenchParameterized","bench1", "str", "3")
        Assertions.assertTrue(b4)

        val b5 = contains(bs, "org.sample.OtherBench", "bench3")
        Assertions.assertTrue(b5)
    }

    @Test
    fun twoBenchs110() {
        val url = this::class.java.classLoader.getResource(JarHelper.jar2BenchsJmh110)
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.path)
        val benchs = f.all()
        if (benchs.isLeft()) {
            Assertions.fail<String>("Could not get benchmarks: ${benchs.left().get()}")
        }

        val bs = benchs.right().get()

        val b1 = contains(bs, "org.sample.BenchNonParameterized", "bench2")
        Assertions.assertTrue(b1)

        val b2 = contains(bs, "org.sample.BenchParameterized","bench1")
        Assertions.assertTrue(b2)
    }

    @Test
    fun threeBenchs110() {
        val url = this::class.java.classLoader.getResource(JarHelper.jar3BenchsJmh110)
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.path)
        val benchs = f.all()
        if (benchs.isLeft()) {
            Assertions.fail<String>("Could not get benchmarks: ${benchs.left().get()}")
        }

        val bs = benchs.right().get()

        val b1 = contains(bs, "org.sample.BenchNonParameterized", "bench2")
        Assertions.assertTrue(b1)

        val b2 = contains(bs, "org.sample.BenchParameterized","bench1")
        Assertions.assertTrue(b2)

        val b5 = contains(bs, "org.sample.OtherBench", "bench3")
        Assertions.assertTrue(b5)
    }
}
