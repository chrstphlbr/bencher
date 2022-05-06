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

object WalaSCTestHelper {

    val exclusionsFile = "wala_exclusions.txt".fileResource()

    fun covered(
        covs: Coverages,
        from: Method, to: Method, level: Int,
        possibly: Boolean = false, probability: Double = 1.0
    ) {
        val cov = covs.coverages[from]
        if (cov == null) {
            Assertions.fail<String>("No benchmark for $from")
            return
        }

        covered(cov, from, to, level, possibly, probability)
    }

    fun covered(
        cov: CoverageComputation,
        from: Method, to: Method, level: Int,
        possibly: Boolean = false, probability: Double = 1.0
    ) {
        val unit = cov.single(from, to)

        if (unit is NotCovered) {
            Assertions.fail<String>("No unit ($to) from bench ($from) covered")
        }

        val l = if (possibly) {
            // possibly expected
            Assertions.assertTrue(unit is PossiblyCovered, "Expected PossiblyCovered but got $unit [$from -> $to]")
            val pc = unit as PossiblyCovered
            Pair(pc.level, pc.probability)
        } else {
            // certainly expected
            Assertions.assertTrue(unit is Covered, "Expected Covered but got $unit [$from -> $to]")
            val c = unit as Covered
            Pair(c.level, 1.0)
        }

        Assertions.assertEquals(level, l.first, "Unexpected level [$from -> $to]: $unit")
        Assertions.assertEquals(probability, roundProb(l.second), "Unexpected probability [$from -> $to]: $unit")
    }

    private fun roundProb(p: Double): Double {
        val nf = "%.2f".format(Locale.ROOT, p)
        return nf.toDouble()
    }

    fun assertCoverages(cov: WalaSC, jar: File): Coverages {
        val covs = cov.get(jar.toPath()).getOrHandle {
            Assertions.fail<String>("Could not get Coverages: $it")
            throw IllegalStateException("should never happen")
        }

        return covs
    }

    fun errStr(unit: String, level: Int): String =
            "coverage of $unit on level $level not found"

    fun cha(jar: String): ClassHierarchy {
        val ef = exclusionsFile
        Assertions.assertTrue(ef.exists(), "Wala-test-exclusions file does not exist")
        val jarFile = jar.fileResource()
        Assertions.assertTrue(jarFile.exists(), "Jar file ($jar) does not exist")
        val jarPath = jarFile.absolutePath

        val scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jarPath, ef)
        return ClassHierarchyFactory.make(scope)
    }

    fun print(cov: Coverages) {
        val p = SimpleCoveragePrinter(System.out)
        p.print(cov)
    }
}
