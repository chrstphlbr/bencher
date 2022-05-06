package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageComputation
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.Covered
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.NotCovered
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.PossiblyCovered

typealias MethodWeights = Map<out Method, Double>

interface MethodWeighter {
    fun weights(mapper: MethodWeightMapper): Either<String, MethodWeights>

    fun weights(): Either<String, MethodWeights> = weights(IdentityMethodWeightMapper)
}

fun coverageUnitWeight(
    method: Method,
    coverage: CoverageComputation,
    methodWeights: MethodWeights,
    exclusions: Set<Method>,
    accumulator: (Double, Double) -> Double = Double::plus
): Pair<Double, Set<Method>> =
        mcwCoveredUnitsFirst(method, coverage, methodWeights, exclusions, accumulator)

// should be internal and should not be used externally-> not possible because of JMH benchmarks that test it
fun mcwUnitWeightsFirst(
    method: Method,
    coverage: CoverageComputation,
    methodWeights: MethodWeights,
    exclusions: Set<Method>,
    accumulator: (Double, Double) -> Double = Double::plus
): Pair<Double, Set<Method>> {
    var nv = 0.0
    val ne = exclusions.toMutableSet()

    methodWeights.asSequence()
            .filter { !exclusions.contains(it.key) }
            .mapNotNull { (to, v) ->
                val r = coverage.single(method, to)
                when (r) {
                    is NotCovered -> null
                    is PossiblyCovered -> Pair(to, v * r.probability)
                    is Covered -> Pair(to, v)
                }
            }
            .forEach {
                ne.add(it.first)
                nv = accumulator(nv, it.second)
            }

    return Pair(nv, ne)
}

// should be internal and should not be used externally-> not possible because of JMH benchmarks that test it
fun mcwCoveredUnitsFirst(
    method: Method,
    coverage: CoverageComputation,
    methodWeights: MethodWeights,
    exclusions: Set<Method>,
    accumulator: (Double, Double) -> Double = Double::plus
): Pair<Double, Set<Method>> {
    var nv = 0.0
    val ne = exclusions.toMutableSet()

    coverage.all(true)
            .asSequence()
            .filter { !exclusions.contains(it.unit) }
            .mapNotNull { r ->
                val mw = methodWeights[r.unit] ?: return@mapNotNull null
                when (r) {
                    is PossiblyCovered -> Pair(r.unit, mw * r.probability)
                    is Covered -> Pair(r.unit, mw)
                    is NotCovered -> null
                }
            }
            .forEach {
                ne.add(it.first)
                nv = accumulator(nv, it.second)
            }

    return Pair(nv, ne)
}
