package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JdtBenchFinderTest : AbstractJdtBenchFinderTest() {

    private fun jmhBenchs(bf: JdtBenchFinder, bs: Iterable<Benchmark>): jmhBenchs =
            bs.map { b ->
                Triple(setOf(b), bf.setups(b).toSet(), bf.tearDowns(b).toSet())
            }

    @Test
    fun twoBenchs121() {
        val url = SourceCodeTestHelper.benchs2Jmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")

        val bf = JdtBenchFinder(url.absoluteFile, pkgPrefix)
        val bs = bf.all().getOrElse {
            Assertions.fail<String>("Could not retrieve benchmarks: $it")
            return
        }
        assertTwoBenchs(bs)
        assertBenchsSetupsTearDowns(jmhBenchs(bf, bs))
        Assertions.assertEquals(2, bs.size)
    }

    @Test
    fun fourBenchs121() {
        val url = SourceCodeTestHelper.benchs4Jmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")

        val bf = JdtBenchFinder(url.absoluteFile, pkgPrefix)
        val bs = bf.all().getOrElse {
            Assertions.fail<String>("Could not retrieve benchmarks: $it")
            return
        }
        assertTwoBenchs(bs)
        assertBenchsSetupsTearDowns(jmhBenchs(bf, bs))
        Assertions.assertEquals(4, bs.size)
    }

    @Test
    fun fourBenchs121NoPp() {
        val url = SourceCodeTestHelper.benchs4Jmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")

        val bf = JdtBenchFinder(url.absoluteFile)
        val bs = bf.all().getOrElse {
            Assertions.fail<String>("Could not retrieve benchmarks: $it")
            return
        }
        assertTwoBenchs(bs)
        assertBenchsSetupsTearDowns(jmhBenchs(bf, bs))
        Assertions.assertEquals(4, bs.size)
    }

    companion object {
        val pkgPrefix = setOf("org.sample", "org.sam")
    }
}
