package ch.uzh.ifi.seal.bencher.analysis.coverage.sta

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchFinder
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class WalaSCExtendedMultiCGTest : WalaSCTest() {

    override val covs: Coverages
        get() = WalaSCExtendedMultiCGTest.cov

    override val multiCGEntrypoints = true

    @Test
    fun nonLibCallsBench1() {
//        sout(bench1, 2, true, 0.5)
        sout(bench1, 2)
    }

    @Test
    fun nonLibCallsBench2() {
        sout(bench2, 2)
    }

    @Test
    fun nonLibCallsBench3() {
        sout(bench3, 2)
    }

    fun sout(bench: Benchmark, level: Int, possibly: Boolean = false, probability: Double = 1.0) {
        val sbNew = PlainMethod(clazz = "java.lang.StringBuilder", name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        h.covered(cov, bench, sbNew, level, possibly, probability)
        val sbAppend = PlainMethod(clazz = "java.lang.StringBuilder", name = "append", params = listOf("java.lang.String"), returnType = SourceCodeConstants.void)
        h.covered(cov, bench, sbAppend, level, possibly, probability)
        val sbToString = PlainMethod(clazz = "java.lang.StringBuilder", name = "toString", params = listOf(), returnType = SourceCodeConstants.void)
        h.covered(cov, bench, sbToString, level, possibly, probability)
        val funPrintln = PlainMethod(clazz = "java.io.PrintStream", name = "println", params = listOf("java.lang.String"), returnType = SourceCodeConstants.void)
        h.covered(cov, bench, funPrintln, level, possibly, probability)
    }

    companion object {
        lateinit var cov: Coverages

        @JvmStatic
        @BeforeAll
        fun setup() {
            val jar = JarTestHelper.jar4BenchsJmh121.fileResource()

            cov = h.assertCoverages(
                    WalaSC(
                            entrypoints = CGEntrypoints(
                                    mf = AsmBenchFinder(jar),
                                    me = BenchmarkWithSetupTearDownEntrypoints(),
                                    ea = MultiCGEntrypoints()
                            ),
                            algo = WalaRTA()
                    ),
                    jar
            )
        }
    }
}
