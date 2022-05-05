package ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability

import ch.uzh.ifi.seal.bencher.Method


sealed class CoverageUnitResult(
        open val unit: Method
)

data class NotCovered(
        override val unit: Method
) : CoverageUnitResult(unit)

data class Covered(
        override val unit: Method,
        val level: Int
) : CoverageUnitResult(unit)

data class PossiblyCovered(
        override val unit: Method,
        val level: Int,
        val probability: Double
) : CoverageUnitResult(unit)
