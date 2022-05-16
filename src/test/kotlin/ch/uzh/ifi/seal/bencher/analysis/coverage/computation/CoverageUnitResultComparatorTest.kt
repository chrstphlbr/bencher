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
    ).toCoverageUnit()

    private val mb = PlainMethod(
            clazz = "b.b.B",
            name = "b",
            params = listOf("a", "b", "c"),
            returnType = SourceCodeConstants.void
    ).toCoverageUnit()

    @Test
    fun notCoveredEquals() {
        val c1 = NotCovered(unit = mb)
        val c2 = NotCovered(unit = mb)
        val cr = CoverageUnitResultComparator.compare(c1, c2)
        Assertions.assertEquals(0, cr)
    }

    @Test
    fun notCoveredToDifferent() {
        val c1 = NotCovered(unit = ma)
        val c2 = NotCovered(unit = mb)
        val cr1 = CoverageUnitResultComparator.compare(c1, c2)
        Assertions.assertTrue(cr1 < 0)
        val cr2 = CoverageUnitResultComparator.compare(c2, c1)
        Assertions.assertTrue(cr2 > 0)
    }

    @Test
    fun coveredEquals() {
        val c1 = Covered(unit = mb, level = 4)
        val c2 = Covered(unit = mb, level = 4)
        val cr = CoverageUnitResultComparator.compare(c1, c2)
        Assertions.assertEquals(0, cr)
    }

    @Test
    fun coveredToDifferent() {
        val c1 = Covered(unit = ma, level = 4)
        val c2 = Covered(unit = mb, level = 4)
        val cr1 = CoverageUnitResultComparator.compare(c1, c2)
        Assertions.assertTrue(cr1 < 0)
        val cr2 = CoverageUnitResultComparator.compare(c2, c1)
        Assertions.assertTrue(cr2 > 0)
    }

    @Test
    fun coveredLevelDifferent() {
        val c1 = Covered(unit = mb, level = 3)
        val c2 = Covered(unit = mb, level = 4)
        val cr1 = CoverageUnitResultComparator.compare(c1, c2)
        Assertions.assertTrue(cr1 < 0)
        val cr2 = CoverageUnitResultComparator.compare(c2, c1)
        Assertions.assertTrue(cr2 > 0)
    }

    @Test
    fun possiblyCoveredEquals() {
        val c1 = PossiblyCovered(unit = mb, level = 4, probability = 0.28)
        val c2 = PossiblyCovered(unit = mb, level = 4, probability = 0.28)
        val cr = CoverageUnitResultComparator.compare(c1, c2)
        Assertions.assertEquals(0, cr)
    }

    @Test
    fun possiblyCoveredToDifferent() {
        val c1 = PossiblyCovered(unit = ma, level = 4, probability = 0.28)
        val c2 = PossiblyCovered(unit = mb, level = 4, probability = 0.28)
        val cr1 = CoverageUnitResultComparator.compare(c1, c2)
        Assertions.assertTrue(cr1 < 0)
        val cr2 = CoverageUnitResultComparator.compare(c2, c1)
        Assertions.assertTrue(cr2 > 0)
    }

    @Test
    fun possiblyCoveredLevelDifferent() {
        val c1 = PossiblyCovered(unit = mb, level = 3, probability = 0.28)
        val c2 = PossiblyCovered(unit = mb, level = 4, probability = 0.28)
        val cr1 = CoverageUnitResultComparator.compare(c1, c2)
        Assertions.assertTrue(cr1 < 0)
        val cr2 = CoverageUnitResultComparator.compare(c2, c1)
        Assertions.assertTrue(cr2 > 0)
    }

    @Test
    fun possiblyCoveredProbabilityDifferent() {
        val c1 = PossiblyCovered(unit = mb, level = 4, probability = 0.71)
        val c2 = PossiblyCovered(unit = mb, level = 4, probability = 0.28)
        val cr1 = CoverageUnitResultComparator.compare(c1, c2)
        Assertions.assertTrue(cr1 < 0)
        val cr2 = CoverageUnitResultComparator.compare(c2, c1)
        Assertions.assertTrue(cr2 > 0)
    }

    @Test
    fun order() {
        val cn = NotCovered(unit = mb)
        val c = Covered(unit = mb, level = 4)
        val cp = PossiblyCovered(unit = mb, level = 4, probability = 1.0)

        // NotCovered vs Covered
        val cr11 = CoverageUnitResultComparator.compare(cn, c)
        Assertions.assertTrue(cr11 > 0)
        val cr12 = CoverageUnitResultComparator.compare(c, cn)
        Assertions.assertTrue(cr12 < 0)

        // PossiblyCovered vs. Covered
        val cr21 = CoverageUnitResultComparator.compare(cp, c)
        Assertions.assertTrue(cr21 > 0)
        val cr22 = CoverageUnitResultComparator.compare(c, cp)
        Assertions.assertTrue(cr22 < 0)

        // NotCovered vs PossiblyCovered
        val cr31 = CoverageUnitResultComparator.compare(cn, cp)
        Assertions.assertTrue(cr31 > 0)
        val cr32 = CoverageUnitResultComparator.compare(cp, cn)
        Assertions.assertTrue(cr32 < 0)
    }
}
