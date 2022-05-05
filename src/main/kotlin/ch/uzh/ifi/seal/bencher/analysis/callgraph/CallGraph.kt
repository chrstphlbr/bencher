package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.CUF
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.Coverage
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.CoverageComputation
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.CoverageUnitResult
import ch.uzh.ifi.seal.bencher.analysis.change.Change
import ch.uzh.ifi.seal.bencher.analysis.change.ChangeAssessment
import ch.uzh.ifi.seal.bencher.analysis.change.FullChangeAssessment


data class CGResult(
        val calls: Map<Method, Coverage>
) : CoverageComputation, CGOverlap by CGOverlapImpl(calls.values) {

    override fun single(of: Method, unit: Method): CoverageUnitResult {
        val mcs = calls[of] ?: return CUF.notCovered(of, unit)
        return mcs.single(of, unit)
    }

    override fun all(removeDuplicates: Boolean): Set<CoverageUnitResult> =
            calls.flatMap { it.value.all(removeDuplicates) }.toSet()

    fun onlyChangedReachabilities(
        changes: Set<Change>,
        changeAssessment: ChangeAssessment = FullChangeAssessment
    ): CGResult {
        val newCG: Map<Method, Coverage> = calls.mapValues { (_, rs) ->
            val newRs = rs.all()
                .filter { changeAssessment.methodChanged(it.unit, changes) }
                .toSet()
            Coverage(of = rs.of, unitResults = newRs)
        }

        return CGResult(newCG)
    }
}

fun Iterable<CGResult>.merge(): CGResult =
        this.fold(CGResult(mapOf())) { acc, cgr -> merge(acc, cgr) }


fun merge(cgr1: CGResult, cgr2: CGResult): CGResult {
    val c1 = cgr1.calls
    val c2 = cgr2.calls
    val intersectingKeys = c1.keys.intersect(c2.keys)
    if (intersectingKeys.isEmpty()) {
        // disjoint set of benchmarks -> return the union of the map
        return CGResult(
                calls = c1 + c2
        )
    }

    // overlapping benchmark sets
    val newCalls = mutableMapOf<Method, Coverage>()
    // bc1 benchmarks that are not in bc2
    newCalls.putAll(c1.filterKeys { intersectingKeys.contains(it) })
    // bc2 benchmarks that are not in bc1
    newCalls.putAll(c2.filterKeys { intersectingKeys.contains(it) })
    // merge of benchmarks that are in both bc1 and bc2
    newCalls.putAll(
            intersectingKeys.map {
                Pair(it, c1.getValue(it).union(c2.getValue(it)))
            }
    )

    return CGResult(
            calls = newCalls
    )
}
