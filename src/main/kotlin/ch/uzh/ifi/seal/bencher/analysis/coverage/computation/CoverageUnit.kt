package ch.uzh.ifi.seal.bencher.analysis.coverage.computation

import ch.uzh.ifi.seal.bencher.Line
import ch.uzh.ifi.seal.bencher.Method

sealed interface CoverageUnit

data class CoverageUnitMethod(
    val method: Method
) : CoverageUnit

fun Method.toCoverageUnit(): CoverageUnitMethod = CoverageUnitMethod(this)

data class CoverageUnitLine(
    val line: Line,
    val missedInstructions: Int? = null,
    val coveredInstructions: Int? = null,
    val missedBranches: Int? = null,
    val coveredBranches: Int? = null
) : CoverageUnit

fun Line.toCoverageUnit(
    missedInstructions: Int?,
    coveredInstructions: Int?,
    missedBranches: Int?,
    coveredBranches: Int?)
: CoverageUnitLine = CoverageUnitLine(
    line = this,
    missedInstructions = missedInstructions,
    coveredInstructions = coveredInstructions,
    missedBranches = missedBranches,
    coveredBranches = coveredBranches
)

enum class CoverageUnitType {
    ALL, METHOD, LINE
}
