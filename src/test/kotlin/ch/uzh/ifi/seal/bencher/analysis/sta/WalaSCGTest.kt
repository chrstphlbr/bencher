package ch.uzh.ifi.seal.bencher.analysis.sta

import ch.uzh.ifi.seal.bencher.analysis.callgraph.SimplePrinter
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaRTA
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaSCG
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

class WalaSCGTest {

    @Test
    fun test() {
        val e = WalaSCG(jarPath, JarBenchFinder(jarPath), WalaRTA())
        val cgRes = e.get()
        if (cgRes.isLeft()) {
            Assertions.fail<String>("Could not get CG: ${cgRes.left().get()}")
        }

        val cg = cgRes.right().get()

        val p = SimplePrinter(System.out)
        p.print(cg)
    }

    companion object {
        private lateinit var jar: File
        private lateinit var jarPath: String

        @BeforeAll
        @JvmStatic
        fun setup() {
            jar = File(this::class.java.classLoader.getResource("benchmarks_3_jmh121.jar").toURI())
            jarPath = jar.absolutePath
        }
    }
}
