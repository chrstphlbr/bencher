package ch.uzh.ifi.seal.bencher.analysis.sta

import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.WalaCGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaRTA
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaSCG
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

class WalaSCGLibOnlyTest : WalaSCGTest() {

    override val cg: CGResult
        get() = WalaSCGLibOnlyTest.cg

    @Test
    fun libOnlyCalls() {
        val justLibCalls = cg.benchCalls.values.flatten().fold(true, { acc, mc ->
            acc && mc.method.clazz.startsWith(pkgPrefix)
        })

        Assertions.assertTrue(justLibCalls, "Non-lib calls in CG")
    }

    companion object {
        val h = WalaSCGTestHelper
        lateinit var cg: WalaCGResult

        val pkgPrefix = "org.sample"

        @JvmStatic
        @BeforeAll
        fun setup() {
            val jar = File(this::class.java.classLoader.getResource("benchmarks_3_jmh121.jar").toURI())
            val jarPath = jar.absolutePath

            cg = h.assertCGResult(WalaSCG(jarPath, JarBenchFinder(jarPath), WalaRTA(), inclusions = IncludeOnly(setOf(pkgPrefix))))
        }
    }
}
