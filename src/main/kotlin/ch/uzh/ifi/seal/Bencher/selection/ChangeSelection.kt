package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.change.Change

interface ChangeSelection {
    fun affected(benchmark: Benchmark, change: Change, cgResult: CGResult): Boolean

    fun affected(benchmark: Benchmark, changes: Iterable<Change>, cgResult: CGResult): Boolean =
           changes.fold(false) { acc, c ->
               acc || affected(benchmark, c, cgResult)
           }

    fun affected(benchmarks: Iterable<Benchmark>, changes: Iterable<Change>, cgResult: CGResult): Iterable<Benchmark> =
            benchmarks.filter { affected(it, changes, cgResult) }

}
