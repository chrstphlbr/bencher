package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

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

        val bf = JdtBenchFinder(url.absoluteFile, prefix)
        val ebs = bf.all()
        if (ebs.isLeft()) {
            Assertions.fail<String>("Could not retrieve benchmarks: ${ebs.left().get()}")
        }
        val bs = ebs.right().get()
        assertTwoBenchs(bs)
        assertBenchsSetupsTearDowns(jmhBenchs(bf, bs))
        Assertions.assertEquals(2, bs.size)
    }

    @Test
    fun fourBenchs121() {
        val url = SourceCodeTestHelper.benchs4Jmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")

        val bf = JdtBenchFinder(url.absoluteFile, prefix)
        val ebs = bf.all()
        if (ebs.isLeft()) {
            Assertions.fail<String>("Could not retrieve benchmarks: ${ebs.left().get()}")
        }
        val bs = ebs.right().get()
        assertTwoBenchs(bs)
        assertBenchsSetupsTearDowns(jmhBenchs(bf, bs))
        Assertions.assertEquals(4, bs.size)
    }

    @Test
    fun fourBenchs121NoPp() {
        val url = SourceCodeTestHelper.benchs4Jmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")

        val bf = JdtBenchFinder(url.absoluteFile)
        val ebs = bf.all()
        if (ebs.isLeft()) {
            Assertions.fail<String>("Could not retrieve benchmarks: ${ebs.left().get()}")
        }
        val bs = ebs.right().get()
        assertTwoBenchs(bs)
        assertBenchsSetupsTearDowns(jmhBenchs(bf, bs))
        Assertions.assertEquals(4, bs.size)
    }

    companion object {
        val prefix = "org.sample"
    }
}
