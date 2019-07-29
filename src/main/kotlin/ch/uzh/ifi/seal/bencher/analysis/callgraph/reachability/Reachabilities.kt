package ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability

import ch.uzh.ifi.seal.bencher.Method

class Reachabilities(
        val start: Method,
        private val reachabilities: Set<ReachabilityResult>
) : Reachability {

    private val reachabilitiesNoDuplicates: Set<ReachabilityResult>
    private val tosRR: Map<Method, ReachabilityResult>

    init {
        val srs = reachabilities.sortedWith(ReachabilityResultComparator)
        val selected = mutableSetOf<Method>()

        val rsnd = mutableSetOf<ReachabilityResult>()
        val mtosrr = mutableMapOf<Method, ReachabilityResult>()

        srs.forEach{
            if (!selected.contains(it.to)) {
                selected.add(it.to)
                rsnd.add(it)
                mtosrr[it.to] = it
            }
        }

        reachabilitiesNoDuplicates = rsnd
        tosRR = mtosrr
    }

    override fun reachable(from: Method, to: Method): ReachabilityResult {
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

    private fun map(from: Method, to: Method, r: ReachabilityResult): ReachabilityResult =
            when (r) {
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
                is NotReachable -> RF.notReachable(
                        from = from,
                        to = to
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
