package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Method

interface Reachability {
    fun reachable(from: Method, to: Method): ReachabilityResult

    fun reachable(from: Method, ms: Iterable<Method>): Iterable<ReachabilityResult> = ms.map { reachable(from, it)}
}

sealed class ReachabilityResult(
        open val from: Method,
        open val to: Method
)
data class NotReachable(
        override val from: Method,
        override val to: Method
) : ReachabilityResult(from, to)
data class Reachable(
        override val from: Method,
        override val to: Method,
        val level: Int
) : ReachabilityResult(from, to)
data class PossiblyReachable(
        override val from: Method,
        override val to: Method,
        val level: Int,
        val probability: Double
) : ReachabilityResult(from, to)
