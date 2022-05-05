package ch.uzh.ifi.seal.bencher.selection

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.change.Change
import ch.uzh.ifi.seal.bencher.analysis.change.FullChangeAssessment

class FullChangeSelector(
        private val cgResult: CGResult,
        private val changes: Set<Change>
) : Selector {

    override fun select(benchs: Iterable<Benchmark>): Either<String, Iterable<Benchmark>> =
            Either.Right(benchs.filter { select(it, changes, cgResult) })

    private fun select(benchmark: Benchmark, changes: Set<Change>, cgResult: CGResult): Boolean =
            changes.any { select(benchmark, it, cgResult) }

    private fun select(benchmark: Benchmark, change: Change, cgResult: CGResult): Boolean =
            FullChangeAssessment.methodChanged(benchmark, change) ||
                    changeInCalledMethod(benchmark, change, cgResult)

    // returns true iff
    //  (1) the benchmark b exists in the call graph,
    //  (2) the change is a MethodChange and this method is reachable from b
    //  (3) the change affects (variable changed, constructor changed) the reachable method
    private fun changeInCalledMethod(b: Benchmark, c: Change, cgResult: CGResult): Boolean {
        // (1)
        val calls = cgResult.calls[b] ?: return false
        // (2), (3)
        return calls.reachabilities(true).any { mc -> FullChangeAssessment.methodChanged(mc.unit, c) }
    }
}
