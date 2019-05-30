package ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability

import ch.uzh.ifi.seal.bencher.Method

interface Reachability {
    fun reachable(from: Method, to: Method): ReachabilityResult
    fun reachable(from: Method, ms: Iterable<Method>, excludeNotReachable: Boolean = true): Iterable<ReachabilityResult> =
            ms.mapNotNull {
                val r = reachable(from, it)
                if (excludeNotReachable && r is NotReachable) {
                    null
                } else {
                    r
                }
            }
    fun reachabilities(removeDuplicateTos: Boolean = false): Set<ReachabilityResult>
}
