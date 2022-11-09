package ch.uzh.ifi.seal.bencher.analysis.coverage.computation

object CoverageUnitResultComparator : Comparator<CoverageUnitResult> {
    private val pc = compareBy(CoverageUnitComparator, PossiblyCovered::unit)
            .thenByDescending(PossiblyCovered::probability)
            .thenBy(PossiblyCovered::level)

    private val nc = compareBy(CoverageUnitComparator, NotCovered::unit)

    private val cc = compareBy(CoverageUnitComparator, Covered::unit)
            .thenBy(Covered::level)


    override fun compare(cur1: CoverageUnitResult, cur2: CoverageUnitResult): Int {
        if (cur1 == cur2) {
            return 0
        }

        if (cur1.unit::class != cur2.unit::class) {
            return CoverageUnitComparator.compare(cur1.unit, cur2.unit)
        }

        if (cur1::class == cur2::class) {
            return compareSameClass(cur1, cur2)
        }

        if (cur1 !is NotCovered && cur2 is NotCovered) {
            return -1
        } else if (cur1 is NotCovered && cur2 !is NotCovered) {
            return 1
        }

        if (cur1 !is PossiblyCovered && cur2 is PossiblyCovered) {
            return -1
        } else if (cur1 is PossiblyCovered && cur2 !is PossiblyCovered) {
            return 1
        }

        if (cur1 !is NotCovered && cur2 is NotCovered) {
            return -1
        } else if (cur1 is NotCovered && cur2 !is NotCovered) {
            return 1
        }

        throw IllegalStateException("Can not handle cur1: ${cur1::class}; cur2: ${cur2::class}")
    }

    private fun compareSameClass(cur1: CoverageUnitResult, cur2: CoverageUnitResult): Int =
            when {
                cur1 is NotCovered && cur2 is NotCovered -> compare(cur1, cur2)
                cur1 is PossiblyCovered && cur2 is PossiblyCovered -> compare(cur1, cur2)
                cur1 is Covered && cur2 is Covered -> compare(cur1, cur2)
                else -> throw IllegalArgumentException("cur1 and cur2 not of same type: ${cur1::class} != ${cur2::class}")
            }

    private fun compare(pc1: PossiblyCovered, pc2: PossiblyCovered): Int = pc.compare(pc1, pc2)

    private fun compare(c1: Covered, c2: Covered): Int = cc.compare(c1, c2)

    private fun compare(nc1: NotCovered, nc2: NotCovered): Int = nc.compare(nc1, nc2)
}
