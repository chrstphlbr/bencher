package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.analysis.finder.AbstractBenchJmhParamSourceTest
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JdtBenchJmhParamSourceTest : AbstractBenchJmhParamSourceTest() {

    @Test
    fun test() {
        val f = JdtBenchFinder(SourceCodeTestHelper.benchs4Jmh121v2.fileResource())

        val benchs = f.all().getOrElse {
            Assertions.fail<String>("Could not load benchmarks: $it")
            return
        }

        assertBenchStateObj(f, benchs)
    }
}