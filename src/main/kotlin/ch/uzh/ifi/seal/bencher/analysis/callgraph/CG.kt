package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Method
import org.jgrapht.Graph
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DirectedWeightedPseudograph

class CG(
        val start: Method,
        private val edges: Set<MethodCall>
) : Reachability, Set<MethodCall> by edges {

    private val adjacencyLists: Map<Method, Set<MethodCall>>
    private val tos: Set<Method>

    private val g: Graph<Method, MethodCall>
    private val sp: ShortestPathAlgorithm<Method, MethodCall>

    init {
        val l = mutableMapOf<Method, Set<MethodCall>>()
        val t = mutableSetOf<Method>()

        val ng = DirectedWeightedPseudograph<Method, MethodCall>(MethodCall::class.java)

        edges.forEach {
            //            ng.addVertex(it.from)
//            ng.addVertex(it.to)
//            ng.addEdge(it.from, it.to, it)
//            ng.setEdgeWeight(it, it.nrPossibleTargets.toDouble())

            // add to reachable methods (tos)
            t.add(it.to)

            // add to adjacency list
            val f = it.from
            if (l.containsKey(f)) {
                l[f] = l[f]!! + setOf(it)
            } else {
                l[f] = setOf(it)
            }
        }

//        ng.edgeSet().forEach { println(it) }

        tos = t
        adjacencyLists = l
        g = ng
        sp = DijkstraShortestPath(g)
    }

//    fun reachable2(from: Method, ms: Iterable<Method>): Iterable<ReachabilityResult> =
//            if (from != start) {
//                ms.map { NotReachable(from, it) }
//            } else {
//                val fp = from.toPlainMethod()
//                val pms = ms.map { it.toPlainMethod() }
//                reachable(from, pms, fp, 1.0, 1, setOf())
//            }
//
//    private fun reachable(start: Method, ends: List<Method>, previous: Method, probability: Double, level: Int, seen: Set<Method>): Set<ReachabilityResult> {
//        return setOf()
//    }

//    override fun reachable(from: Method, ms: Iterable<Method>): Iterable<ReachabilityResult> {
//        val aps = AllDirectedPaths<Method, MethodCall>(g)
//
//        val paths = aps.getAllPaths(setOf(from.toPlainMethod()), ms.toSet(), true, null)
//
//    }

    override fun reachable(from: Method, to: Method): ReachabilityResult = reachableOwn(from, to)

    private fun reachableJGraphT(from: Method, to: Method): ReachabilityResult {
        val f = from.toPlainMethod()
        val t = to.toPlainMethod()
        if (!g.containsVertex(f) || !g.containsVertex(t)) {
            return NotReachable(f, t)
        }

        val pathElements = sp.getPath(f, t) ?: return NotReachable(f, t)

        val level = pathElements.length
        val prob = pathElements.edgeList.fold(1.0) { acc, mc -> independantProbability(acc, mc) }

        return if (prob == 1.0) {
            Reachable(
                    from = from,
                    to = to,
                    level = level
            )
        } else {
            PossiblyReachable(
                    from = from,
                    to = to,
                    level = level,
                    probability = prob
            )
        }
    }

    private fun reachableOwn(from: Method, to: Method): ReachabilityResult =
            if (from != start) {
                NotReachable(from, to)
            } else if (!tos.contains(to)) {
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

    fun union(other: CG): CG =
            if (start != other.start) {
                throw IllegalArgumentException("Can not create union: start vertices not equal ($start != ${other.start})")
            } else {
                CG(
                        start = start,
                        edges = edges.union(other.edges)
                )
            }

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
