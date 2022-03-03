package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import arrow.core.getOrHandle
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.SimpleCGPrinter
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.NotReachable
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.PossiblyReachable
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Reachability
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Reachable
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.util.config.AnalysisScopeReader
import org.junit.jupiter.api.Assertions
import java.io.File
import java.util.*

object WalaSCGTestHelper {

    val exclusionsFile = "wala_exclusions.txt".fileResource()

    fun reachable(
            cgr: CGResult,
            from: Method, to: Method, level: Int,
            possibly: Boolean = false, probability: Double = 1.0
    ) {
        val cg = cgr.calls[from]
        if (cg == null) {
            Assertions.fail<String>("No benchmark for $from")
            return
        }

        reachable(cg, from, to, level, possibly, probability)
    }

    fun reachable(
            cg: Reachability,
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
            Pair(r.level, 1.0)
        }

        Assertions.assertEquals(level, l.first, "Unexpected level [$from -> $to]: $rr")
        Assertions.assertEquals(probability, roundProb(l.second), "Unexpected probability [$from -> $to]: $rr")
    }

    private fun roundProb(p: Double): Double {
        val nf = "%.2f".format(Locale.ROOT, p)
        return nf.toDouble()
    }

    fun assertCGResult(wcg: WalaSCG, jar: File): CGResult {
        val cgRes = wcg.get(jar.toPath()).getOrHandle {
            Assertions.fail<String>("Could not get CG: $it")
            throw IllegalStateException("should never happen")
        }

        return cgRes
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
