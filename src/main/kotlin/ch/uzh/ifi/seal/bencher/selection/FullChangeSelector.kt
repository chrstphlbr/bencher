package ch.uzh.ifi.seal.bencher.selection

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.change.Change
import ch.uzh.ifi.seal.bencher.analysis.change.FullChangeAssessment

class FullChangeSelector(
    private val coverages: Coverages,
    private val changes: Set<Change>
) : Selector {

    override fun select(benchs: Iterable<Benchmark>): Either<String, Iterable<Benchmark>> =
            Either.Right(benchs.filter { select(it, changes, coverages) })

    private fun select(benchmark: Benchmark, changes: Set<Change>, coverages: Coverages): Boolean =
            changes.any { select(benchmark, it, coverages) }

    private fun select(benchmark: Benchmark, change: Change, coverages: Coverages): Boolean =
            FullChangeAssessment.methodChanged(benchmark, change) ||
                    changeInCalledMethod(benchmark, change, coverages)

    // returns true iff
    //  (1) the benchmark b exists in the call graph,
    //  (2) the change is a MethodChange and this method is reachable from b
    //  (3) the change affects (variable changed, constructor changed) the reachable method
    private fun changeInCalledMethod(b: Benchmark, c: Change, coverages: Coverages): Boolean {
        // (1)
        val calls = coverages.coverages[b] ?: return false
        // (2), (3)
        return calls.all(true).any { mc -> FullChangeAssessment.methodChanged(mc.unit, c) }
    }
}
