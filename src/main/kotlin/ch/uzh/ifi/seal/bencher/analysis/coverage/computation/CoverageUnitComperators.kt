package ch.uzh.ifi.seal.bencher.analysis.coverage.computation

import ch.uzh.ifi.seal.bencher.LineComparator
import ch.uzh.ifi.seal.bencher.MethodComparator


object CoverageUnitComparator : Comparator<CoverageUnit> {
    override fun compare(cu1: CoverageUnit, cu2: CoverageUnit): Int =
        if (cu1 is CoverageUnitMethod && cu2 is CoverageUnitMethod) {
            CoverageUnitMethodComparator.compare(cu1, cu2)
        } else if (cu1 is CoverageUnitLine && cu2 is CoverageUnitLine) {
            CoverageUnitLineComparator.compare(cu1, cu2)
        } else if (cu1 is CoverageUnitMethod) {
            // order Methods before Lines
            -1
        } else {
            // order Lines after Methods
            1
        }
}

object CoverageUnitMethodComparator : Comparator<CoverageUnitMethod> {
    override fun compare(cu1: CoverageUnitMethod, cu2: CoverageUnitMethod): Int =
        MethodComparator.compare(cu1.method, cu2.method)
}

object CoverageUnitLineComparator : Comparator<CoverageUnitLine> {
    val c = compareBy(LineComparator, CoverageUnitLine::line)
        .thenByDescending(CoverageUnitLine::coveredInstructions)
        .thenByDescending(CoverageUnitLine::coveredBranches)
        .thenBy(CoverageUnitLine::missedInstructions)
        .thenBy(CoverageUnitLine::missedBranches)

    override fun compare(cu1: CoverageUnitLine, cu2: CoverageUnitLine): Int = c.compare(cu1, cu2)
}
