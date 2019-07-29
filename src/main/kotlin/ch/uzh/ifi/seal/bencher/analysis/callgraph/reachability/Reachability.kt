package ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability

import ch.uzh.ifi.seal.bencher.Method

interface Reachability {
    fun reachable(from: Method, to: Method): ReachabilityResult
    fun reachabilities(removeDuplicateTos: Boolean = false): Set<ReachabilityResult>
}
