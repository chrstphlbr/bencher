package ch.uzh.ifi.seal.bencher.analysis.coverage.computation

import ch.uzh.ifi.seal.bencher.Line
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

    private val la = Line(
        file = "A.java",
        number = 1
    ).toCoverageUnit(null, null, null, null)

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

    @Test
    fun orderMethodLineCovered() {
        val m = Covered(unit = ma, level = 1)
        val l = Covered(unit = la, level = 1)

        val ml = CoverageUnitResultComparator.compare(m, l)
        Assertions.assertTrue(ml < 0)
        val lm = CoverageUnitResultComparator.compare(l, m)
        Assertions.assertTrue(lm > 0)
    }

    @Test
    fun orderMethodLineNotCovered() {
        val m = NotCovered(unit = ma)
        val l = NotCovered(unit = la)

        val ml = CoverageUnitResultComparator.compare(m, l)
        Assertions.assertTrue(ml < 0)
        val lm = CoverageUnitResultComparator.compare(l, m)
        Assertions.assertTrue(lm > 0)
    }

    @Test
    fun orderMethodLinePossiblyCovered() {
        val m = PossiblyCovered(unit = ma, level = 1, probability = 0.5)
        val l = PossiblyCovered(unit = la, level = 1, probability = 0.5)

        val ml = CoverageUnitResultComparator.compare(m, l)
        Assertions.assertTrue(ml < 0)
        val lm = CoverageUnitResultComparator.compare(l, m)
        Assertions.assertTrue(lm > 0)
    }

    @Test
    fun orderMethodLineCoveredNotCovered() {
        val m1 = Covered(unit = ma, level = 1)
        val l1 = NotCovered(unit = la)

        val ml1 = CoverageUnitResultComparator.compare(m1, l1)
        Assertions.assertTrue(ml1 < 0)
        val lm1 = CoverageUnitResultComparator.compare(l1, m1)
        Assertions.assertTrue(lm1 > 0)

        val m2 = NotCovered(unit = ma)
        val l2 = Covered(unit = la, level = 1)

        val ml2 = CoverageUnitResultComparator.compare(m2, l2)
        Assertions.assertTrue(ml2 < 0)
        val lm2 = CoverageUnitResultComparator.compare(l2, m2)
        Assertions.assertTrue(lm2 > 0)
    }

    @Test
    fun orderMethodLineCoveredPossiblyCovered() {
        val m1 = Covered(unit = ma, level = 1)
        val l1 = PossiblyCovered(unit = la, level = 1, probability = 0.5)

        val ml1 = CoverageUnitResultComparator.compare(m1, l1)
        Assertions.assertTrue(ml1 < 0)
        val lm1 = CoverageUnitResultComparator.compare(l1, m1)
        Assertions.assertTrue(lm1 > 0)

        val m2 = PossiblyCovered(unit = ma, level = 1, probability = 0.5)
        val l2 = Covered(unit = la, level = 1)

        val ml2 = CoverageUnitResultComparator.compare(m2, l2)
        Assertions.assertTrue(ml2 < 0)
        val lm2 = CoverageUnitResultComparator.compare(l2, m2)
        Assertions.assertTrue(lm2 > 0)
    }

    @Test
    fun orderMethodLinePossiblyCoveredNotCovered() {
        val m1 = NotCovered(unit = ma)
        val l1 = PossiblyCovered(unit = la, level = 1, probability = 0.5)

        val ml1 = CoverageUnitResultComparator.compare(m1, l1)
        Assertions.assertTrue(ml1 < 0)
        val lm1 = CoverageUnitResultComparator.compare(l1, m1)
        Assertions.assertTrue(lm1 > 0)

        val m2 = PossiblyCovered(unit = ma, level = 1, probability = 0.5)
        val l2 = NotCovered(unit = la)

        val ml2 = CoverageUnitResultComparator.compare(m2, l2)
        Assertions.assertTrue(ml2 < 0)
        val lm2 = CoverageUnitResultComparator.compare(l2, m2)
        Assertions.assertTrue(lm2 > 0)
    }
}
