package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JarBenchFinderTest {

    @Test
    fun twoBenchs121() {
        val url = JarTestHelper.jar2BenchsJmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.absoluteFile)
        val benchs = f.all()
        if (benchs.isLeft()) {
            Assertions.fail<String>("Could not get benchmarks: ${benchs.left().get()}")
        }

        val bs = benchs.right().get()

        val b1 = FinderTestHelper.contains(bs, bench2)
        Assertions.assertTrue(b1)

        FinderTestHelper.assertParamTest(bs, bench1)
    }

    @Test
    fun fourBenchs121() {
        val url = JarTestHelper.jar4BenchsJmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.absoluteFile)
        val benchs = f.all()
        if (benchs.isLeft()) {
            Assertions.fail<String>("Could not get benchmarks: ${benchs.left().get()}")
        }

        val bs = benchs.right().get()

        val b1 = FinderTestHelper.contains(bs, bench2)
        Assertions.assertTrue(b1)

        FinderTestHelper.assertParamTest(bs, bench1)

        val b5 = FinderTestHelper.contains(bs, bench3)
        Assertions.assertTrue(b5)

        FinderTestHelper.assertParamTest(bs, bench4)
    }

    @Test
    fun twoBenchs110() {
        val url = JarTestHelper.jar2BenchsJmh110.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.absoluteFile)
        val benchs = f.all()
        if (benchs.isLeft()) {
            Assertions.fail<String>("Could not get benchmarks: ${benchs.left().get()}")
        }

        val bs = benchs.right().get()

        val b1 = FinderTestHelper.contains(bs, bench2)
        Assertions.assertTrue(b1)

        val b2 = FinderTestHelper.contains(bs, bench1)
        Assertions.assertTrue(b2)
    }

    @Test
    fun fourBenchs110() {
        val url = JarTestHelper.jar4BenchsJmh110.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.absoluteFile)
        val benchs = f.all()
        if (benchs.isLeft()) {
            Assertions.fail<String>("Could not get benchmarks: ${benchs.left().get()}")
        }

        val bs = benchs.right().get()

        val b1 = FinderTestHelper.contains(bs, bench2)
        Assertions.assertTrue(b1)

        val b2 = FinderTestHelper.contains(bs, bench1)
        Assertions.assertTrue(b2)

        val b3 = FinderTestHelper.contains(bs, bench3)
        Assertions.assertTrue(b3)

        val b4 = FinderTestHelper.contains(bs, bench4)
        Assertions.assertTrue(b4)
    }

    companion object {
        val bench1 = JarTestHelper.BenchParameterized.bench1
        val bench2 = JarTestHelper.BenchNonParameterized.bench2
        val bench3 = JarTestHelper.OtherBench.bench3
        val bench4 = JarTestHelper.BenchParameterized2.bench4
    }
}
