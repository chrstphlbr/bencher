package ch.uzh.ifi.seal.bencher.analysis.coverage.sta

import arrow.core.getOrHandle
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.SimpleCoveragePrinter
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageComputation
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.Covered
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.NotCovered
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.PossiblyCovered
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
        cgr: Coverages,
        from: Method, to: Method, level: Int,
        possibly: Boolean = false, probability: Double = 1.0
    ) {
        val cg = cgr.coverages[from]
        if (cg == null) {
            Assertions.fail<String>("No benchmark for $from")
            return
        }

        reachable(cg, from, to, level, possibly, probability)
    }

    fun reachable(
        cg: CoverageComputation,
        from: Method, to: Method, level: Int,
        possibly: Boolean = false, probability: Double = 1.0
    ) {
        val rr = cg.single(from, to)

        if (rr is NotCovered) {
            Assertions.fail<String>("No method call ($to) from bench ($from) reachable")
        }

        val l = if (possibly) {
            // possibly expected
            Assertions.assertTrue(rr is PossiblyCovered, "Expected PossiblyReachable but got $rr [$from -> $to]")
            val pr = rr as PossiblyCovered
            Pair(pr.level, pr.probability)
        } else {
            // certainly expected
            Assertions.assertTrue(rr is Covered, "Expected Reachable but got $rr [$from -> $to]")
            val r = rr as Covered
            Pair(r.level, 1.0)
        }

        Assertions.assertEquals(level, l.first, "Unexpected level [$from -> $to]: $rr")
        Assertions.assertEquals(probability, roundProb(l.second), "Unexpected probability [$from -> $to]: $rr")
    }

    private fun roundProb(p: Double): Double {
        val nf = "%.2f".format(Locale.ROOT, p)
        return nf.toDouble()
    }

    fun assertCGResult(wcg: WalaSCG, jar: File): Coverages {
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

    fun print(cg: Coverages) {
        val p = SimpleCoveragePrinter(System.out)
        p.print(cg)
    }
}
