package ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability

import ch.uzh.ifi.seal.bencher.MethodComparator

object ReachabilityResultComparator : Comparator<CoverageUnitResult> {
    private val pc = compareBy(MethodComparator, PossiblyCovered::unit)
            .thenByDescending(PossiblyCovered::probability)
            .thenBy(PossiblyCovered::level)

    private val nc = compareBy(MethodComparator, NotCovered::unit)

    private val rc = compareBy(MethodComparator, Covered::unit)
            .thenBy(Covered::level)


    override fun compare(r1: CoverageUnitResult, r2: CoverageUnitResult): Int {
        if (r1 == r2) {
            return 0
        }

        if (r1::class == r2::class) {
            return compareSameClass(r1, r2)
        }

        if (r1 !is NotCovered && r2 is NotCovered) {
            return -1
        } else if (r1 is NotCovered && r2 !is NotCovered) {
            return 1
        }

        if (r1 !is PossiblyCovered && r2 is PossiblyCovered) {
            return -1
        } else if (r1 is PossiblyCovered && r2 !is PossiblyCovered) {
            return 1
        }

        if (r1 !is NotCovered && r2 is NotCovered) {
            return -1
        } else if (r1 is NotCovered && r2 !is NotCovered) {
            return 1
        }

        throw IllegalStateException("Can not handle r1: ${r1::class}; r2: ${r2::class}")
    }

    private fun compareSameClass(r1: CoverageUnitResult, r2: CoverageUnitResult): Int =
            when {
                r1 is NotCovered && r2 is NotCovered -> compare(r1, r2)
                r1 is PossiblyCovered && r2 is PossiblyCovered -> compare(r1, r2)
                r1 is Covered && r2 is Covered -> compare(r1, r2)
                else -> throw IllegalArgumentException("r1 and r2 not of same type: ${r1::class} != ${r2::class}")
            }

    private fun compare(r1: PossiblyCovered, r2: PossiblyCovered): Int = pc.compare(r1, r2)

    private fun compare(r1: Covered, r2: Covered): Int = rc.compare(r1, r2)

    private fun compare(r1: NotCovered, r2: NotCovered): Int = nc.compare(r1, r2)
}
