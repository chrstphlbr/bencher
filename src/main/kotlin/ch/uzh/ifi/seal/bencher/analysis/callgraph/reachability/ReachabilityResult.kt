package ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability

import ch.uzh.ifi.seal.bencher.Method


sealed class ReachabilityResult(
        open val to: Method
)

data class NotReachable(
        override val to: Method
) : ReachabilityResult(to)

data class Reachable(
        override val to: Method,
        val level: Int
) : ReachabilityResult(to)

data class PossiblyReachable(
        override val to: Method,
        val level: Int,
        val probability: Double
) : ReachabilityResult(to)
