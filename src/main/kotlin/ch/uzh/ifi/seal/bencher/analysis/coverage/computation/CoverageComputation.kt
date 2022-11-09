package ch.uzh.ifi.seal.bencher.analysis.coverage.computation

import ch.uzh.ifi.seal.bencher.Method

interface CoverageComputation {
    fun single(of: Method, unit: CoverageUnit): CoverageUnitResult
    fun all(removeDuplicates: Boolean = false): Set<CoverageUnitResult>
}
