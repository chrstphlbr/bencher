package ch.uzh.ifi.seal.bencher.analysis.coverage.computation

import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CoverageUnitResultComparatorTest {

    private val ma = PlainMethod(
            clazz = "a.a.A",
            name = "a",
            params = listOf("a", "b", "c"),
            returnType = SourceCodeConstants.void
    )

    private val mb = PlainMethod(
            clazz = "b.b.B",
            name = "b",
            params = listOf("a", "b", "c"),
            returnType = SourceCodeConstants.void
    )

    @Test
    fun notReachableEquals() {
        val r1 = NotCovered(unit = mb)
        val r2 = NotCovered(unit = mb)
        val cr = CoverageUnitResultComparator.compare(r1, r2)
        Assertions.assertEquals(0, cr)
    }

    @Test
    fun notReachableToDifferent() {
        val r1 = NotCovered(unit = ma)
        val r2 = NotCovered(unit = mb)
        val cr1 = CoverageUnitResultComparator.compare(r1, r2)
        Assertions.assertTrue(cr1 < 0)
        val cr2 = CoverageUnitResultComparator.compare(r2, r1)
        Assertions.assertTrue(cr2 > 0)
    }

    @Test
    fun reachableEquals() {
        val r1 = Covered(unit = mb, level = 4)
        val r2 = Covered(unit = mb, level = 4)
        val cr = CoverageUnitResultComparator.compare(r1, r2)
        Assertions.assertEquals(0, cr)
    }

    @Test
    fun reachableToDifferent() {
        val r1 = Covered(unit = ma, level = 4)
        val r2 = Covered(unit = mb, level = 4)
        val cr1 = CoverageUnitResultComparator.compare(r1, r2)
        Assertions.assertTrue(cr1 < 0)
        val cr2 = CoverageUnitResultComparator.compare(r2, r1)
        Assertions.assertTrue(cr2 > 0)
    }

    @Test
    fun reachableLevelDifferent() {
        val r1 = Covered(unit = mb, level = 3)
        val r2 = Covered(unit = mb, level = 4)
        val cr1 = CoverageUnitResultComparator.compare(r1, r2)
        Assertions.assertTrue(cr1 < 0)
        val cr2 = CoverageUnitResultComparator.compare(r2, r1)
        Assertions.assertTrue(cr2 > 0)
    }

    @Test
    fun possiblyReachableEquals() {
        val r1 = PossiblyCovered(unit = mb, level = 4, probability = 0.28)
        val r2 = PossiblyCovered(unit = mb, level = 4, probability = 0.28)
        val cr = CoverageUnitResultComparator.compare(r1, r2)
        Assertions.assertEquals(0, cr)
    }

    @Test
    fun possiblyReachableToDifferent() {
        val r1 = PossiblyCovered(unit = ma, level = 4, probability = 0.28)
        val r2 = PossiblyCovered(unit = mb, level = 4, probability = 0.28)
        val cr1 = CoverageUnitResultComparator.compare(r1, r2)
        Assertions.assertTrue(cr1 < 0)
        val cr2 = CoverageUnitResultComparator.compare(r2, r1)
        Assertions.assertTrue(cr2 > 0)
    }

    @Test
    fun possiblyReachableLevelDifferent() {
        val r1 = PossiblyCovered(unit = mb, level = 3, probability = 0.28)
        val r2 = PossiblyCovered(unit = mb, level = 4, probability = 0.28)
        val cr1 = CoverageUnitResultComparator.compare(r1, r2)
        Assertions.assertTrue(cr1 < 0)
        val cr2 = CoverageUnitResultComparator.compare(r2, r1)
        Assertions.assertTrue(cr2 > 0)
    }

    @Test
    fun possiblyReachableProbabilityDifferent() {
        val r1 = PossiblyCovered(unit = mb, level = 4, probability = 0.71)
        val r2 = PossiblyCovered(unit = mb, level = 4, probability = 0.28)
        val cr1 = CoverageUnitResultComparator.compare(r1, r2)
        Assertions.assertTrue(cr1 < 0)
        val cr2 = CoverageUnitResultComparator.compare(r2, r1)
        Assertions.assertTrue(cr2 > 0)
    }

    @Test
    fun order() {
        val rn = NotCovered(unit = mb)
        val rr = Covered(unit = mb, level = 4)
        val rp = PossiblyCovered(unit = mb, level = 4, probability = 1.0)

        // NotReachable vs Reachable
        val cr11 = CoverageUnitResultComparator.compare(rn, rr)
        Assertions.assertTrue(cr11 > 0)
        val cr12 = CoverageUnitResultComparator.compare(rr, rn)
        Assertions.assertTrue(cr12 < 0)

        // PossiblyReachable vs. Reachable
        val cr21 = CoverageUnitResultComparator.compare(rp, rr)
        Assertions.assertTrue(cr21 > 0)
        val cr22 = CoverageUnitResultComparator.compare(rr, rp)
        Assertions.assertTrue(cr22 < 0)

        // NotReachable vs PossiblyReachable
        val cr31 = CoverageUnitResultComparator.compare(rn, rp)
        Assertions.assertTrue(cr31 > 0)
        val cr32 = CoverageUnitResultComparator.compare(rp, rn)
        Assertions.assertTrue(cr32 < 0)
    }
}
