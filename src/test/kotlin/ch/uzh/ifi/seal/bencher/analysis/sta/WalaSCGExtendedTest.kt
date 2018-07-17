package ch.uzh.ifi.seal.bencher.analysis.sta

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.WalaCGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaRTA
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaSCG
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

class WalaSCGExtendedTest : WalaSCGTest() {

    override val cg: CGResult
        get() = WalaSCGExtendedTest.cg

    @Test
    fun nonLibCallsBench1() {
        sout(h.bench1, 2)
    }

    @Test
    fun nonLibCallsBench2() {
        sout(h.bench2, 2)
    }

    @Test
    fun nonLibCallsBench3() {
        sout(h.bench3, 2)
    }

    fun sout(bench: Benchmark, level: Int) {
        val sbNew = PlainMethod(clazz = "java.lang.StringBuilder", name = "<init>", params = listOf())
        h.reachable(cg, bench, sbNew, level)
        val sbAppend = PlainMethod(clazz = "java.lang.StringBuilder", name = "append", params = listOf("java.lang.String"))
        h.reachable(cg, bench, sbAppend, level)
        val sbToString = PlainMethod(clazz = "java.lang.StringBuilder", name = "toString", params = listOf())
        h.reachable(cg, bench, sbToString, level)
        val funPrintln = PlainMethod(clazz = "java.io.PrintStream", name = "println", params = listOf("java.lang.String"))
        h.reachable(cg, bench, funPrintln, level)
    }

    companion object {
        lateinit var cg: WalaCGResult

        @JvmStatic
        @BeforeAll
        fun setup() {
            val jar = File(this::class.java.classLoader.getResource("benchmarks_3_jmh121.jar").toURI())
            val jarPath = jar.absolutePath

            cg = h.assertCGResult(WalaSCG(jarPath, JarBenchFinder(jarPath), WalaRTA()))
        }
    }
}
