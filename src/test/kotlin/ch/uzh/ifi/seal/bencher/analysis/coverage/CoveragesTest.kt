package ch.uzh.ifi.seal.bencher.analysis.coverage

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.NotCovered
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.toCoverageUnit
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CoveragesTest {

    @Test
    fun mergeEmpty() {
        val covs: Iterable<Coverages> = listOf()
        val merged = covs.merge()
        Assertions.assertEquals(Coverages(mapOf()), merged)
    }

    @Test
    fun mergeOne() {
        val covs = Coverages(mapOf())
        val covsList = listOf(covs)
        val merged = covsList.merge()
        val expected = Coverages(mapOf())
        Assertions.assertEquals(expected, merged)
    }

    @Test
    fun mergeSingle() {
        val covs = Coverages(mapOf(b1Cov, b2Cov, b3Cov))
        val covsList = listOf(covs)
        val merged = covsList.merge()
        Assertions.assertEquals(expectedCoverages, merged)
    }

    @Test
    fun mergeMultiDisjoint() {
        val covs1 = Coverages(mapOf(b1Cov))
        val covs2 = Coverages(mapOf(b2Cov))
        val covs3 = Coverages(mapOf(b3Cov))
        val covsList = listOf(covs1, covs2, covs3)
        val merged = covsList.merge()
        Assertions.assertEquals(expectedCoverages, merged)
    }

    @Test
    fun mergeMultiOverlapping() {
        val cov1 = Coverages(mapOf(b1Cov, b2Cov))
        val cov2 = Coverages(mapOf(b2Cov, b3Cov))
        val cov3 = Coverages(mapOf(b1Cov, b3Cov))
        val covList = listOf(cov1, cov2, cov3)
        val merged = covList.merge()

        Assertions.assertEquals(expectedCoverages, merged)

        expectedCoverages.coverages.forEach { (b, coverage) ->
            Assertions.assertTrue(merged.coverages.containsKey(b), "Merged Coverages does not contain benchmark $b")

            val mergedCoverages = merged.coverages[b]
            Assertions.assertNotNull(mergedCoverages)

            coverage.all().forEach { c ->
                val cc = mergedCoverages!!.all().contains(c)
                Assertions.assertTrue(cc, "Merged Coverages for bench ($b) does not contain coverage ($c)")
            }
        }
    }

    @Test
    fun covered() {
        val cov = Coverages(mapOf(b1Cov))
        val ca = cov.single(JarTestHelper.BenchParameterized.bench1, JarTestHelper.CoreA.m.toCoverageUnit())
        Assertions.assertFalse(ca is NotCovered)
        val cb = cov.single(JarTestHelper.BenchParameterized.bench1, JarTestHelper.CoreB.m.toCoverageUnit())
        Assertions.assertFalse(cb is NotCovered)
    }

    @Test
    fun notCovered() {
        val cov = Coverages(mapOf(b1Cov))
        val cd = cov.single(JarTestHelper.BenchParameterized.bench1, JarTestHelper.CoreD.m.toCoverageUnit())
        Assertions.assertTrue(cd is NotCovered)
    }

    @Test
    fun multipleCovered() {
        val cov = Coverages(mapOf(b1Cov))

        listOf(JarTestHelper.CoreA.m, JarTestHelper.CoreB.m).forEach { m ->
            val c = cov.single(JarTestHelper.BenchParameterized.bench1, m.toCoverageUnit())
            Assertions.assertFalse(c is NotCovered)
        }
    }

    @Test
    fun multipleNotCovered() {
        val cov = Coverages(mapOf(b2Cov))

        listOf(JarTestHelper.CoreA.m, JarTestHelper.CoreB.m).forEach { m ->
            val c = cov.single(JarTestHelper.BenchParameterized.bench1, m.toCoverageUnit())
            Assertions.assertTrue(c is NotCovered)
        }
    }

    companion object {
        private val b1Cov = CoveragesTestHelper.b1MethodCov
        private val b2Cov = CoveragesTestHelper.b2MethodCov
        private val b3Cov = CoveragesTestHelper.b3MethodCov
        private val expectedCoverages = Coverages(mapOf(b1Cov, b2Cov, b3Cov))
    }
}
