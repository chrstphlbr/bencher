package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.*

typealias CoverageUnitWeights = Map<out CoverageUnit, Double>

fun coverageUnitWeight(
    method: Method,
    coverage: CoverageComputation,
    coverageUnitWeights: CoverageUnitWeights,
    exclusions: Set<CoverageUnit>,
    accumulator: (Double, Double) -> Double = Double::plus
): Pair<Double, Set<CoverageUnit>> =
        cuwCoveredUnitsFirst(method, coverage, coverageUnitWeights, exclusions, accumulator)

// should be internal and should not be used externally-> not possible because of JMH benchmarks that test it
internal fun cuwUnitWeightsFirst(
    method: Method,
    coverage: CoverageComputation,
    coverageUnitWeights: CoverageUnitWeights,
    exclusions: Set<CoverageUnit>,
    accumulator: (Double, Double) -> Double = Double::plus
): Pair<Double, Set<CoverageUnit>> {
    var nv = 0.0
    val ne = exclusions.toMutableSet()

    coverageUnitWeights.asSequence()
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
internal fun cuwCoveredUnitsFirst(
    method: Method,
    coverage: CoverageComputation,
    coverageUnitWeights: CoverageUnitWeights,
    exclusions: Set<CoverageUnit>,
    accumulator: (Double, Double) -> Double = Double::plus
): Pair<Double, Set<CoverageUnit>> {
    var nv = 0.0
    val ne = exclusions.toMutableSet()

    coverage.all(true)
            .asSequence()
            .filter { !exclusions.contains(it.unit) }
            .mapNotNull { r ->
                val mw = coverageUnitWeights[r.unit] ?: return@mapNotNull null
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
