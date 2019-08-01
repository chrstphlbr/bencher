package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JdtBenchExecInfoFinderTest : AbstractJdtBenchExecInfoTest() {

    @Test
    fun test() {
        val f = JdtBenchFinder(SourceCodeTestHelper.benchs4Jmh121v2.fileResource())

        val eClassExecInfos = f.classExecutionInfos()
        if (eClassExecInfos.isLeft()) {
            Assertions.fail<String>("Could not load class execution infos: ${eClassExecInfos.left().get()}")
        }

        val classExecInfos = eClassExecInfos.right().get()

        assertClassConfigs(classExecInfos)

        val eBenchExecInfos = f.benchmarkExecutionInfos()
        if (eBenchExecInfos.isLeft()) {
            Assertions.fail<String>("Could not load benchmark execution infos: ${eBenchExecInfos.left().get()}")
        }

        val benchExecInfos = eBenchExecInfos.right().get()

        assertBenchConfigs(benchExecInfos)
    }
}
