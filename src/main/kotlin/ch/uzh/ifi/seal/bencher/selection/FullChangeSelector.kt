package ch.uzh.ifi.seal.bencher.selection

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.change.Change
import ch.uzh.ifi.seal.bencher.analysis.change.FullMethodChangeAssessment
import ch.uzh.ifi.seal.bencher.analysis.change.LineChangeAssessmentImpl
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitLine
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitMethod

class FullChangeSelector(
    private val coverages: Coverages,
    private val changes: Set<Change>
) : Selector {

    override fun select(benchs: Iterable<Benchmark>): Either<String, Iterable<Benchmark>> =
            Either.Right(benchs.filter { select(it, changes, coverages) })

    private fun select(benchmark: Benchmark, changes: Set<Change>, coverages: Coverages): Boolean =
            changes.any { select(benchmark, it, coverages) }

    private fun select(benchmark: Benchmark, change: Change, coverages: Coverages): Boolean =
            FullMethodChangeAssessment.methodChanged(benchmark, change) ||
                    changeInCalledMethod(benchmark, change, coverages)

    // returns true iff
    //  (1) the benchmark b exists in the call graph,
    //  (2) the change is a MethodChange and this method is covered by b
    //  (3) the change affects (variable changed, constructor changed) the covered method
    //  (4) the change is a LineChange and the benchmark b covers the line
    private fun changeInCalledMethod(b: Benchmark, c: Change, coverages: Coverages): Boolean {
        // (1)
        val cov = coverages.coverages[b] ?: return false
        // (2), (3)
        return cov.all(true).any { mc ->
            when (val u = mc.unit) {
                is CoverageUnitMethod -> FullMethodChangeAssessment.methodChanged(u.method, c)
                is CoverageUnitLine -> LineChangeAssessmentImpl.lineChanged(u.line, c)
            }
        }
    }
}
