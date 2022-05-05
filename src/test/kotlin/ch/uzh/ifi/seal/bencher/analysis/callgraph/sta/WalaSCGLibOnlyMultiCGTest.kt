package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchFinder
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class WalaSCGLibOnlyMultiCGTest : WalaSCGTest() {

    override val cgr: CGResult
        get() = WalaSCGLibOnlyMultiCGTest.cg

    override val multiCGEntrypoints = true

    @Test
    fun libOnlyCalls() {
        val justLibCalls = cg.reachabilities().fold(true) { acc, mc ->
            acc && mc.unit.clazz.startsWith(pkgPrefix)
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

            cg = h.assertCGResult(
                    WalaSCG(
                            entrypoints = CGEntrypoints(
                                    mf = AsmBenchFinder(jar),
                                    me = BenchmarkWithSetupTearDownEntrypoints(),
                                    ea = MultiCGEntrypoints()
                            ),
                            algo = WalaRTA(),
                            inclusions = IncludeOnly(setOf(pkgPrefix))
                    ),
                    jar = jar
            )
        }
    }
}
