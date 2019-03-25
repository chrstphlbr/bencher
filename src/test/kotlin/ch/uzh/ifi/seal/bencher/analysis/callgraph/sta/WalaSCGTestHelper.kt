package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.*
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.util.config.AnalysisScopeReader
import org.junit.jupiter.api.Assertions
import java.io.File

object WalaSCGTestHelper {

    val exclusionsFile = "wala_exclusions.txt".fileResource()

    fun reachable(
            cgr: CGResult,
            from: Method, to: Method, level: Int,
            possibly: Boolean = false, probability: Double = 1.0
    ) {
        val cg = cgr.calls.get(from)
        if (cg == null) {
            Assertions.fail<String>("No benchmark for $from")
            return
        }

        reachable(cg, from, to, level, possibly, probability)
    }

    fun reachable(
            cg: CG,
            from: Method, to: Method, level: Int,
            possibly: Boolean = false, probability: Double = 1.0
    ) {
        val rr = cg.reachable(from, to)

        if (rr is NotReachable) {
            Assertions.fail<String>("No method call ($to) from bench ($from) reachable")
        }

        val l = if (possibly) {
            // possibly expected
            Assertions.assertTrue(rr is PossiblyReachable, "Expected PossiblyReachable but got $rr [$from -> $to]")
            val pr = rr as PossiblyReachable
            Pair(pr.level, pr.probability)
        } else {
            // certainly expected
            Assertions.assertTrue(rr is Reachable, "Expected Reachable but got $rr [$from -> $to]")
            val r = rr as Reachable
            Pair(rr.level, 1.0)
        }

        Assertions.assertEquals(level, l.first, "Expected level $level, but was ${l.first} [$from -> $to]")
        Assertions.assertEquals(probability, roundProb(l.second), "Expected probability $probability, but was ${l.second} [$from -> $to]")
    }

    private fun roundProb(p: Double): Double {
        val nf = "%.2f".format(p)
        return nf.toDouble()
    }

    fun assertCGResult(wcg: WalaSCG, jar: File): CGResult {
        val cgRes = wcg.get(jar.toPath())
        if (cgRes.isLeft()) {
            Assertions.fail<String>("Could not get CG: ${cgRes.left().get()}")
        }

        return cgRes.right().get()
    }

    fun errStr(call: String, level: Int): String =
            "call to $call on level $level not found"

    fun cha(jar: String): ClassHierarchy {
        val ef = WalaSCGTestHelper.exclusionsFile
        Assertions.assertTrue(ef.exists(), "Wala-test-exclusions file does not exist")
        val jarFile = jar.fileResource()
        Assertions.assertTrue(jarFile.exists(), "Jar file ($jar) does not exist")
        val jarPath = jarFile.absolutePath

        val scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jarPath, ef)
        return ClassHierarchyFactory.make(scope)
    }

    fun print(cg: CGResult) {
        val p = SimpleCGPrinter(System.out)
        p.print(cg)
    }
}
