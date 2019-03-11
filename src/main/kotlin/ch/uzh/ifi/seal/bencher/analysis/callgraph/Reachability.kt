package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Method

interface Reachability {
    fun reachable(from: Method, to: Method): Boolean

    fun anyReachable(from: Method, ms: Iterable<Method>): Boolean =
            ms.fold(false) { acc, to -> acc || reachable(from, to) }
}
