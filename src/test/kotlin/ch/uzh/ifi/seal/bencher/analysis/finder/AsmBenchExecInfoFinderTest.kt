package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AsmBenchExecInfoFinderTest : AbstractAsmBenchExecInfoTest() {

    @Test
    fun test() {
        val f = AsmBenchFinder(
                jar = JarTestHelper.jar4BenchsJmh121v2.fileResource(),
                pkgPrefix = "org/sample"
        )

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
