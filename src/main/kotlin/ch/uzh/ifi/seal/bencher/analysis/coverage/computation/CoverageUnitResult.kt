package ch.uzh.ifi.seal.bencher.analysis.coverage.computation


sealed class CoverageUnitResult(
        open val unit: CoverageUnit
)

data class NotCovered(
        override val unit: CoverageUnit
) : CoverageUnitResult(unit)

data class Covered(
        override val unit: CoverageUnit,
        val level: Int
) : CoverageUnitResult(unit)

data class PossiblyCovered(
        override val unit: CoverageUnit,
        val level: Int,
        val probability: Double
) : CoverageUnitResult(unit)
