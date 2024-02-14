package ch.uzh.ifi.seal.bencher.analysis.finder

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JarBenchFinderTest {

    private val javaSettings = JarTestHelper.javaSettings

    @Test
    fun twoBenchs121() {
        val url = JarTestHelper.jar2BenchsJmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.toPath(), javaSettings)

        val bs = f.all().getOrElse {
            Assertions.fail<String>("Could not get benchmarks: $it")
            return
        }

        Assertions.assertEquals(2, bs.size)

        FinderTestHelper.assertParamTest(bs, bench2)
        FinderTestHelper.assertParamTest(bs, bench1)
    }

    @Test
    fun fourBenchs121() {
        val url = JarTestHelper.jar4BenchsJmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.toPath(), javaSettings)

        val bs = f.all().getOrElse {
            Assertions.fail<String>("Could not get benchmarks: $it")
            return
        }

        Assertions.assertEquals(4, bs.size)

        FinderTestHelper.assertParamTest(bs, bench1)
        FinderTestHelper.assertParamTest(bs, bench2)
        FinderTestHelper.assertParamTest(bs, bench3)
        FinderTestHelper.assertParamTest(bs, bench4)
    }

    private fun benchs121v2(removeDuplicates: Boolean) {
        val url = JarTestHelper.jar4BenchsJmh121v2.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.toPath(), javaSettings, removeDuplicates)

        val bs = f.all().getOrElse {
            Assertions.fail<String>("Could not get benchmarks: $it")
            return
        }

        val expectedBenchs = if (removeDuplicates) {
            14
        } else {
            15
        }
        Assertions.assertEquals(expectedBenchs, bs.size)

        FinderTestHelper.assertParamTest(bs, bench1)
        FinderTestHelper.assertParamTest(bs, bench2)
        FinderTestHelper.assertParamTest(bs, bench3)
        FinderTestHelper.assertParamTest(bs, bench4v2)
        FinderTestHelper.assertParamTest(bs, nbbench11)
        FinderTestHelper.assertParamTest(bs, nbbench12)
        FinderTestHelper.assertParamTest(bs, nbbench2)
        FinderTestHelper.assertParamTest(bs, nbbench31)
        FinderTestHelper.assertParamTest(bs, nbbench321)
    }

    @Test
    fun benchs121v2WithoutDuplicates() {
        benchs121v2(false)
    }

    @Test
    fun benchs121v2WithDuplicates() {
        benchs121v2(true)
    }

    @Test
    fun twoBenchs110() {
        val url = JarTestHelper.jar2BenchsJmh110.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.toPath(), javaSettings)

        val bs = f.all().getOrElse {
            Assertions.fail<String>("Could not get benchmarks: $it")
            return
        }

        Assertions.assertEquals(2, bs.size)

        FinderTestHelper.assertParamTest(bs, bench1, withJmhParams = false)
        FinderTestHelper.assertParamTest(bs, bench2, withJmhParams = false)
    }

    @Test
    fun fourBenchs110() {
        val url = JarTestHelper.jar4BenchsJmh110.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")
        val f = JarBenchFinder(url.toPath(), javaSettings)

        val bs = f.all().getOrElse {
            Assertions.fail<String>("Could not get benchmarks: $it")
            return
        }

        Assertions.assertEquals(4, bs.size)

        FinderTestHelper.assertParamTest(bs, bench1, withJmhParams = false)
        FinderTestHelper.assertParamTest(bs, bench2, withJmhParams = false)
        FinderTestHelper.assertParamTest(bs, bench3, withJmhParams = false)
        FinderTestHelper.assertParamTest(bs, bench4, withJmhParams = false)
    }

    companion object {
        val bench1 = JarTestHelper.BenchParameterized.bench1
        val bench2 = JarTestHelper.BenchNonParameterized.bench2
        val bench3 = JarTestHelper.OtherBench.bench3
        val bench4 = JarTestHelper.BenchParameterized2.bench4
        val bench4v2 = JarTestHelper.BenchParameterized2v2.bench4
        val nbbench2 = JarTestHelper.NestedBenchmark.bench2
        val nbbench11 = JarTestHelper.NestedBenchmark.Bench1.bench11
        val nbbench12 = JarTestHelper.NestedBenchmark.Bench1.bench12
        val nbbench31 = JarTestHelper.NestedBenchmark.Bench3.bench31
        val nbbench321 = JarTestHelper.NestedBenchmark.Bench3.Bench32.bench321
    }
}
