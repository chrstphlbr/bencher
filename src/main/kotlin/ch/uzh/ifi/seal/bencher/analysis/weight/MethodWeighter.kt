package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.NotReachable
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.PossiblyReachable
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Reachability
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Reachable
import org.funktionale.either.Either

typealias MethodWeights = Map<out Method, Double>

interface MethodWeighter {
    fun weights(mapper: MethodWeightMapper): Either<String, MethodWeights>

    fun weights(): Either<String, MethodWeights> = weights(IdentityMethodWeightMapper)
}

fun methodCallWeight(
        method: Method,
        reachability: Reachability,
        methodWeights: MethodWeights,
        exclusions: Set<Method>,
        accumulator: (Double, Double) -> Double = Double::plus
): Pair<Double, Set<Method>> =
        imperativeMethodCallWeight(method, reachability, methodWeights, exclusions, accumulator)

private fun functionalMethodCallWeight(
        method: Method,
        reachability: Reachability,
        methodWeights: MethodWeights,
        exclusions: Set<Method>,
        accumulator: (Double, Double) -> Double = Double::plus
): Pair<Double, Set<Method>> =
        reachability.reachable(method, methodWeights.keys).fold(Pair(0.0, exclusions)) { acc, rr ->
            if (acc.second.contains(rr.to)) {
                return@fold acc
            }

            val w = methodWeights[rr.to] ?: 0.0
            val ne = setOf(rr.to.toPlainMethod())

            val v: Pair<Double, Set<Method>> = when (rr) {
                is NotReachable -> Pair(0.0, setOf())
                is PossiblyReachable -> Pair(w * rr.probability, ne)
                is Reachable -> Pair(w, ne)
            }
            Pair(
                    accumulator(acc.first, v.first),
                    acc.second + v.second
            )
        }

private fun imperativeMethodCallWeight(
        method: Method,
        reachability: Reachability,
        methodWeights: MethodWeights,
        exclusions: Set<Method>,
        accumulator: (Double, Double) -> Double = Double::plus
): Pair<Double, Set<Method>> {
    val rs = reachability.reachable(method, methodWeights.keys).filter { !exclusions.contains(it.to) }
    if (rs.isEmpty()) {
        return Pair(0.0, exclusions)
    }

    var w = 0.0
    val seen = exclusions.toMutableSet()

    for (r in rs) {
        if (r is NotReachable || seen.contains(r.to)) {
            continue
        }

        val nw = methodWeights[r.to] ?: 0.0

        when (r) {
            is PossiblyReachable -> w = accumulator(w, nw * r.probability)
            is Reachable -> w = accumulator(w, nw)
        }

        seen.add(r.to.toPlainMethod())
    }

    return Pair(w, seen)
}
