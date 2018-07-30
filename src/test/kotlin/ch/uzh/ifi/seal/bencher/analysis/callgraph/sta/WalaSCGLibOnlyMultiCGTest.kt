package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class WalaSCGLibOnlyMultiCGTest : WalaSCGTest() {

    override val cg: CGResult
        get() = WalaSCGLibOnlyMultiCGTest.cg

    override val multiCGEntrypoints = true

    @Test
    fun libOnlyCalls() {
        val justLibCalls = cg.benchCalls.values.flatten().fold(true) { acc, mc ->
            acc && mc.method.clazz.startsWith(pkgPrefix)
        }

        Assertions.assertTrue(justLibCalls, "Non-lib calls in CG")
    }

    companion object {
        val h = WalaSCGTestHelper
        lateinit var cg: CGResult

        val pkgPrefix = "org.sample"

        @JvmStatic
        @BeforeAll
        fun setup() {
            val jar = JarTestHelper.jar4BenchsJmh121.fileResource()
            val jarPath = jar.absolutePath

            cg = h.assertCGResult(
                    WalaSCG(
                            jar = jarPath,
                            entrypoints = CGEntrypoints(
                                    mf = JarBenchFinder(jarPath),
                                    me = BenchmarkWithSetupTearDownEntrypoints(),
                                    ea = MultiCGEntrypoints()
                            ),
                            algo = WalaRTA(),
                            inclusions = IncludeOnly(setOf(pkgPrefix))
                    )
            )
        }
    }
}
