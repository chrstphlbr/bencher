package ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability

import ch.uzh.ifi.seal.bencher.Method

class Reachabilities(
        val start: Method,
        private val reachabilities: Set<ReachabilityResult>
) : Reachability {

    private val reachabilitiesNoDuplicates: Set<ReachabilityResult> = {
        val srs = reachabilities.sortedWith(ReachabilityResultComparator)
        val selected = mutableSetOf<Method>()
        srs.filter {
            if (selected.contains(it.to)) {
                false
            } else {
                selected.add(it.to)
                true
            }
        }.toSet()
    }()

    private val tosRR: Map<Method, ReachabilityResult> = reachabilitiesNoDuplicates.associateBy { it.to }

    override fun reachable(from: Method, to: Method): ReachabilityResult {
        if (from != start || !tosRR.containsKey(to)) {
            return RF.notReachable(from, to)
        }

        val rr = tosRR[to]
        return map(from, rr) ?: RF.notReachable(from, to)
    }

    private fun map(from: Method, r: ReachabilityResult?): ReachabilityResult? =
            when (r) {
                null -> null
                is NotReachable -> null
                is Reachable -> RF.reachable(
                        from = from,
                        to = r.to,
                        level = r.level
                )
                is PossiblyReachable -> RF.possiblyReachable(
                        from = from,
                        to = r.to,
                        level = r.level,
                        probability = r.probability
                )
            }

    override fun reachabilities(removeDuplicateTos: Boolean): Set<ReachabilityResult> =
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
