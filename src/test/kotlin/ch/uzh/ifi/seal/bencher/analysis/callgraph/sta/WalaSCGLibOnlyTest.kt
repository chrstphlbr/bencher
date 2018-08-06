package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class WalaSCGLibOnlyTest : WalaSCGTest() {

    override val cg: CGResult
        get() = WalaSCGLibOnlyTest.cg

    override val multiCGEntrypoints = false

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

            cg = h.assertCGResult(
                    WalaSCG(
                            entrypoints = CGEntrypoints(
                                    mf = JarBenchFinder(jar.toPath()),
                                    me = BenchmarkWithSetupTearDownEntrypoints(),
                                    ea = SingleCGEntrypoints()
                            ),
                            algo = WalaRTA(),
                            inclusions = IncludeOnly(setOf(pkgPrefix))
                    ),
                    jar = jar
            )
        }
    }
}
