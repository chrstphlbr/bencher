package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.finder.AbstractBenchJmhParamSourceTest
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AsmBenchJmhParamSourceTest : AbstractBenchJmhParamSourceTest() {

    @Test
    fun test() {
        val f = AsmBenchFinder(
                jar = JarTestHelper.jar4BenchsJmh121v2.fileResource(),
                pkgPrefixes = pkgPrefixes
        )

        val eBenchs = f.all()
        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not load benchmarks: ${eBenchs.left().get()}")
        }

        val benchs = eBenchs.right().get()

        assertBenchStateObj(f, benchs)
    }

    companion object {
        val pkgPrefixes = setOf("org.sample", "org.sam")
    }
}
