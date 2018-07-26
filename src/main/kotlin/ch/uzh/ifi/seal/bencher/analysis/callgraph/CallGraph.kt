package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import com.ibm.wala.ipa.callgraph.CallGraph

typealias BenchmarkCalls = Map<Benchmark, Iterable<MethodCall>>

data class MethodCall(
        val method: Method,
        val level: Int
)

data class CGResult(
        val benchCalls: BenchmarkCalls
) : Reachability {
    override fun reachable(b: Benchmark, m: Method): Boolean {
        val mcs = benchCalls[b] ?: return false
        return mcs.map { m == it.method }.reduce { acc, r -> acc || r }
    }
}

fun Iterable<CGResult>.merge(): CGResult =
        this.reduce { acc, cgr -> merge(acc, cgr) }


fun merge(cgr1: CGResult, cgr2: CGResult): CGResult {
    val bc1 = cgr1.benchCalls
    val bc2 = cgr2.benchCalls
    val intersectingKeys = bc1.keys.intersect(bc2.keys)
    if (intersectingKeys.isEmpty()) {
        // disjoint set of benchmarks -> return the union of the map
        return CGResult(
                benchCalls = bc1 + bc2
        )
    }

    // overlapping benchmark sets
    val newBenchCalls = mutableMapOf<Benchmark, Iterable<MethodCall>>()
    // bc1 benchmarks that are not in bc2
    newBenchCalls.putAll(bc1.filterKeys { intersectingKeys.contains(it) })
    // bc2 benchmarks that are not in bc1
    newBenchCalls.putAll(bc2.filterKeys { intersectingKeys.contains(it) })
    // merge of benchmarks that are in both bc1 and bc2
    newBenchCalls.putAll(
            intersectingKeys.map {
                Pair(it, bc1.getValue(it).union(bc2.getValue(it)))
            }
    )

    return CGResult(
            benchCalls = newBenchCalls
    )
}
