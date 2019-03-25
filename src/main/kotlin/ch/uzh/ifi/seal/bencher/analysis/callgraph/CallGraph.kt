package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.MethodComparator

class CG(
        val start: Method,
        val edges: Set<MethodCall>
) : Reachability, Set<MethodCall> by edges {

    private val adjacencyLists: Map<Method, Set<MethodCall>>

    init {
        val l = mutableMapOf<Method, Set<MethodCall>>()

        edges.forEach {
            val f = it.from
            if (l.containsKey(f)) {
                l[f] = l[f]!! + setOf(it)
            } else {
                l[f] = setOf(it)
            }
        }

        adjacencyLists = l
    }

    override fun reachable(from: Method, to: Method): ReachabilityResult =
            if (from != start) {
                NotReachable(from, to)
            } else {
                val fp = from.toPlainMethod()
                reachable(from, to.toPlainMethod(), fp, 1.0, 1, setOf())
            }

    private fun reachable(start: Method, end: Method, previous: Method, probability: Double, level: Int, seen: Set<Method>): ReachabilityResult {
        if (seen.contains(previous)) {
            return NotReachable(start, end)
        }

        val tos = adjacencyLists[previous] ?: return NotReachable(start, end)

        // directly reachable?
        val dr = tos
                .filter { previous == it.from && end == it.to }
                .sortedBy { it.nrPossibleTargets }
                .firstOrNull()

        return if (dr != null) {
            val np = newProbability(probability, dr)
            if (np == 1.0) {
                Reachable(
                        from = start,
                        to = end,
                        level = level
                )
            } else {
                PossiblyReachable(
                        from = start,
                        to = end,
                        level = level,
                        probability = np
                )
            }
        } else {
            val newSeen = seen + setOf(previous)
            val reachabilityResults = tos
                    .map { reachable(start, end, it.to, newProbability(probability, it), level + 1, newSeen) }
                    .filter { it !is NotReachable }

            if (reachabilityResults.isEmpty()) {
                NotReachable(start, end)
            } else {
                certainlyReachable(reachabilityResults) ?: possiblyReachable(reachabilityResults) ?: NotReachable(start, end)
            }
        }
    }

    private fun certainlyReachable(reachable: List<ReachabilityResult>): Reachable? =
            reachable
                .filter { it is Reachable }
                .map { it as Reachable }
                .sortedBy { it.level }
                .firstOrNull()

    private fun possiblyReachable(reachable: List<ReachabilityResult>): PossiblyReachable? =
            reachable
                    .filter { it is PossiblyReachable }
                    .map { it as PossiblyReachable }
                    .sortedByDescending { it.probability }
                    .firstOrNull()

    private fun newProbability(old: Double, mc: MethodCall): Double = independantProbability(old, mc)

    private fun independantProbability(old: Double, mc: MethodCall): Double =
            old * (1.0/mc.nrPossibleTargets)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CG

        if (start != other.start) return false
        if (edges != other.edges) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + edges.hashCode()
        return result
    }

    override fun toString(): String {
        return "CG(start=$start, edges=$edges)"
    }
}

data class MethodCall(
        val from: Method,
        val to: Method,
        val nrPossibleTargets: Int,
        val idPossibleTargets: Int
)

object MethodCallComparator : Comparator<MethodCall> {
    private val c = compareBy(MethodComparator, MethodCall::from)
            .thenBy(MethodCall::idPossibleTargets)
            .thenBy(MethodComparator, MethodCall::to)
            .thenBy(MethodCall::nrPossibleTargets)

    override fun compare(mc1: MethodCall, mc2: MethodCall): Int = c.compare(mc1, mc2)
}

data class CGResult(
        val calls: Map<Method, CG>
) : Reachability {
    override fun reachable(from: Method, to: Method): ReachabilityResult {
        val mcs = calls[from] ?: return NotReachable(from, to)
        return mcs.reachable(from, to)
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
    val newCalls = mutableMapOf<Method, CG>()
    // bc1 benchmarks that are not in bc2
    newCalls.putAll(c1.filterKeys { intersectingKeys.contains(it) })
    // bc2 benchmarks that are not in bc1
    newCalls.putAll(c2.filterKeys { intersectingKeys.contains(it) })
    // merge of benchmarks that are in both bc1 and bc2
    newCalls.putAll(
            intersectingKeys.map {
                Pair(it, CG(
                        start = it,
                        edges = c1.getValue(it).edges.union(c2.getValue(it).edges))
                )
            }
    )

    return CGResult(
            calls = newCalls
    )
}
