package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.NotReachable
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.PossiblyReachable
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Reachability
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Reachable

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
        mcwReachabilitiesFirst(method, reachability, methodWeights, exclusions, accumulator)

// should be internal and should not be used externally-> not possible because of JMH benchmarks that test it
fun mcwMethodWeightsFirst(
        method: Method,
        reachability: Reachability,
        methodWeights: MethodWeights,
        exclusions: Set<Method>,
        accumulator: (Double, Double) -> Double = Double::plus
): Pair<Double, Set<Method>> {
    var nv = 0.0
    val ne = exclusions.toMutableSet()

    methodWeights.asSequence()
            .filter { !exclusions.contains(it.key) }
            .mapNotNull { (to, v) ->
                val r = reachability.reachable(method, to)
                when (r) {
                    is NotReachable -> null
                    is PossiblyReachable -> Pair(to, v * r.probability)
                    is Reachable -> Pair(to, v)
                }
            }
            .forEach {
                ne.add(it.first)
                nv = accumulator(nv, it.second)
            }

    return Pair(nv, ne)
}

// should be internal and should not be used externally-> not possible because of JMH benchmarks that test it
fun mcwReachabilitiesFirst(
        method: Method,
        reachability: Reachability,
        methodWeights: MethodWeights,
        exclusions: Set<Method>,
        accumulator: (Double, Double) -> Double = Double::plus
): Pair<Double, Set<Method>> {
    var nv = 0.0
    val ne = exclusions.toMutableSet()

    reachability.reachabilities(true)
            .asSequence()
            .filter { !exclusions.contains(it.to) }
            .mapNotNull { r ->
                val mw = methodWeights[r.to] ?: return@mapNotNull null
                when (r) {
                    is PossiblyReachable -> Pair(r.to, mw * r.probability)
                    is Reachable -> Pair(r.to, mw)
                    is NotReachable -> null
                }
            }
            .forEach {
                ne.add(it.first)
                nv = accumulator(nv, it.second)
            }

    return Pair(nv, ne)
}
