package ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability

import ch.uzh.ifi.seal.bencher.Method

class Reachabilities(
        val start: Method,
        private val reachabilities: Set<CoverageUnitResult>
) : Reachability {

    private val reachabilitiesNoDuplicates: Set<CoverageUnitResult>
    private val tosRR: Map<Method, CoverageUnitResult>

    init {
        val srs = reachabilities.sortedWith(ReachabilityResultComparator)
        val selected = mutableSetOf<Method>()

        val rsnd = mutableSetOf<CoverageUnitResult>()
        val mtosrr = mutableMapOf<Method, CoverageUnitResult>()

        srs.forEach {
            if (!selected.contains(it.unit)) {
                selected.add(it.unit)
                rsnd.add(it)
                mtosrr[it.unit] = it
            }
        }

        reachabilitiesNoDuplicates = rsnd
        tosRR = mtosrr
    }

    override fun reachable(from: Method, to: Method): CoverageUnitResult {
        if (from != start || !tosRR.containsKey(to)) {
            return RF.notReachable(from, to)
        }

        val rr = tosRR[to]
        return if (rr != null) {
            map(from, to, rr)
        } else {
            RF.notReachable(from, to)
        }
    }

    private fun map(from: Method, to: Method, r: CoverageUnitResult): CoverageUnitResult =
            when (r) {
                is Covered -> RF.reachable(
                        from = from,
                        to = r.unit,
                        level = r.level
                )
                is PossiblyCovered -> RF.possiblyReachable(
                        from = from,
                        to = r.unit,
                        level = r.level,
                        probability = r.probability
                )
                is NotCovered -> RF.notReachable(
                        from = from,
                        to = to
                )
            }

    override fun reachabilities(removeDuplicateTos: Boolean): Set<CoverageUnitResult> =
            if (!removeDuplicateTos) {
                reachabilities
            } else {
                reachabilitiesNoDuplicates
            }

    fun union(other: Reachabilities): Reachabilities =
            if (start != other.start) {
                throw IllegalArgumentException("Can not create union: start vertices not equal ($start != ${other.start})")
            } else {
                Reachabilities(
                        start = start,
                        reachabilities = reachabilities.union(other.reachabilities)
                )
            }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Reachabilities

        if (start != other.start) return false
        if (reachabilities != other.reachabilities) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + reachabilities.hashCode()
        return result
    }

    override fun toString(): String {
        return "Reachabilities(start=$start, reachabilities=$reachabilities)"
    }
}
