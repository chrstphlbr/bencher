package ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability

import ch.uzh.ifi.seal.bencher.Method

interface Reachability {
    fun reachable(from: Method, to: Method): CoverageUnitResult
    fun reachabilities(removeDuplicateTos: Boolean = false): Set<CoverageUnitResult>
}
