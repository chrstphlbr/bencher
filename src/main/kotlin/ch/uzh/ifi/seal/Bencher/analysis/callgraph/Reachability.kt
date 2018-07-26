package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method

interface Reachability {
    fun reachable(b: Benchmark, m: Method): Boolean

    fun anyReachable(b: Benchmark, ms: Iterable<Method>): Boolean =
            ms.fold(false) { acc, m -> acc || reachable(b, m) }
}
