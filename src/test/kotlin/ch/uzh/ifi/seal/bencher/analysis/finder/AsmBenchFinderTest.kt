package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AsmBenchFinderTest : AbstractAsmBenchFinderTest() {

    @Test
    fun twoBenchs121() {
        val url = JarTestHelper.jar2BenchsJmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")

        val bf = AsmBenchFinder(url.absolutePath, pkgPrefix)
        val ebs = bf.all()
        if (ebs.isLeft()) {
            Assertions.fail<String>("Could not retrieve benchmarks: ${ebs.left().get()}")
        }
        val bs = ebs.right().get()
        assertTwoBenchs(bs)
    }

    @Test
    fun fourBenchs121() {
        val url = JarTestHelper.jar4BenchsJmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")

        val bf = AsmBenchFinder(url.absolutePath, pkgPrefix)
        val ebs = bf.all()
        if (ebs.isLeft()) {
            Assertions.fail<String>("Could not retrieve benchmarks: ${ebs.left().get()}")
        }
        val bs = ebs.right().get()
        assertTwoBenchs(bs)
    }

    @Test
    fun twoBenchs110() {
        val url = JarTestHelper.jar2BenchsJmh110.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")

        val bf = AsmBenchFinder(url.absolutePath, pkgPrefix)
        val ebs = bf.all()
        if (ebs.isLeft()) {
            Assertions.fail<String>("Could not retrieve benchmarks: ${ebs.left().get()}")
        }
        val bs = ebs.right().get()
        assertTwoBenchs(bs)
    }

    @Test
    fun fourBenchs110() {
        val url = JarTestHelper.jar4BenchsJmh110.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")

        val bf = AsmBenchFinder(url.absolutePath, pkgPrefix)
        val ebs = bf.all()
        if (ebs.isLeft()) {
            Assertions.fail<String>("Could not retrieve benchmarks: ${ebs.left().get()}")
        }
        val bs = ebs.right().get()
        assertTwoBenchs(bs)
    }

    companion object {
        val pkgPrefix = "org/sample"
    }
}
