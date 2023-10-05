package ch.uzh.ifi.seal.bencher.analysis.coverage.sta

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.*
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.core.util.config.AnalysisScopeReader
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
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
        of: Method, method: Method, level: Int,
        possibly: Boolean = false, probability: Double = 1.0
    ) {
        val unit = cov.single(of, method.toCoverageUnit())

        if (unit is NotCovered) {
            Assertions.fail<String>("No unit ($method) from bench ($of) covered")
        }

        val l = if (possibly) {
            // possibly expected
            Assertions.assertTrue(unit is PossiblyCovered, "Expected PossiblyCovered but got $unit [$of -> $method]")
            val pc = unit as PossiblyCovered
            Pair(pc.level, pc.probability)
        } else {
            // certainly expected
            Assertions.assertTrue(unit is Covered, "Expected Covered but got $unit [$of -> $method]")
            val c = unit as Covered
            Pair(c.level, 1.0)
        }

        Assertions.assertEquals(level, l.first, "Unexpected level [$of -> $method]: $unit")
        Assertions.assertEquals(probability, roundProb(l.second), "Unexpected probability [$of -> $method]: $unit")
    }

    private fun roundProb(p: Double): Double {
        val nf = "%.2f".format(Locale.ROOT, p)
        return nf.toDouble()
    }

    fun assertCoverages(cov: WalaSC, jar: File): Coverages {
        val covs = cov.get(jar.toPath()).getOrElse {
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

        val scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(jarPath, ef)
        return ClassHierarchyFactory.make(scope)
    }
}
