package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import com.ibm.wala.ipa.cha.ClassHierarchy
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class BenchmarkWithSetupTearDownEntrypointsTest {

    @Test
    fun benchOnly() {
        val eps = eg.entrypoints(cha, JarTestHelper.BenchNonParameterized.bench2).toList()
        EntrypointTestHelper.validateEntrypoints(eps, EntrypointTestHelper.BenchNonParameterized.entrypoints)
    }

    @Test
    fun benchAndSetup() {
        val eps = eg.entrypoints(cha, JarTestHelper.BenchParameterized.bench1).toList()
        EntrypointTestHelper.validateEntrypoints(eps, EntrypointTestHelper.BenchParameterized.entrypoints)
    }

    @Test
    fun benchSetupAndTearDown() {
        val eps = eg.entrypoints(cha, JarTestHelper.OtherBench.bench3).toList()
        EntrypointTestHelper.validateEntrypoints(eps, EntrypointTestHelper.OtherBench.entrypoints)
    }

    companion object {
        val eg = BenchmarkWithSetupTearDownEntrypoints()

        lateinit var cha: ClassHierarchy

        @BeforeAll
        @JvmStatic
        fun setup() {
            cha = WalaSCGTestHelper.cha(JarTestHelper.jar4BenchsJmh121)
        }
    }
}
