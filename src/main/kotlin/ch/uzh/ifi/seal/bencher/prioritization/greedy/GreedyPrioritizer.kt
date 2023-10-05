package ch.uzh.ifi.seal.bencher.prioritization.greedy

import arrow.core.firstOrNone
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.MF
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.*
import ch.uzh.ifi.seal.bencher.analysis.weight.CoverageUnitWeights
import ch.uzh.ifi.seal.bencher.analysis.weight.coverageUnitWeight
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import ch.uzh.ifi.seal.bencher.prioritization.Prioritizer
import ch.uzh.ifi.seal.bencher.prioritization.Priority
import ch.uzh.ifi.seal.bencher.prioritization.PrioritySingle
import org.apache.logging.log4j.LogManager

abstract class GreedyPrioritizer(
    private val coverages: Coverages,
    private val coverageUnitWeights: CoverageUnitWeights,
) : Prioritizer {

    protected fun benchValue(b: Benchmark, alreadySelected: Set<CoverageUnit>): Pair<PrioritizedMethod<Benchmark>, Set<CoverageUnit>> {
        val calls = calls(b)
        val p = if (calls == null) {
            Pair(
                    PrioritizedMethod(
                            method = b,
                            priority = Priority(
                                    rank = -1,
                                    total = -1,
                                    value = PrioritySingle(0.0)
                            )
                    ),
                    alreadySelected
            )
        } else {
            val value = coverageUnitWeight(
                    method = b,
                    coverage = calls,
                    coverageUnitWeights = coverageUnitWeights,
                    exclusions = alreadySelected,
                    accumulator = Double::plus
            )

            Pair(
                    PrioritizedMethod(
                            method = b,
                            priority = Priority(
                                    rank = 0,
                                    total = 0,
                                    value = PrioritySingle(value.first)
                            )
                    ),
                    value.second
            )
        }

        return p
    }

    private fun calls(b: Benchmark): Coverage? {
        val exactCalls = coverages.coverages[b]
        return exactCalls ?: callsByClassAndMethod(b) ?: callsByGroup(b)
    }

    private fun callsByClassAndMethod(b: Benchmark): Coverage? {
        // find the coverages that matches class name and method name of the benchmark
        val covs = coverages.coverages.filterKeys {
            it.clazz == b.clazz && it.name == b.name
        }

        val covsSize = covs.size

        return covs.entries
            .firstOrNone()
            .map { (key, value) ->
                if (covsSize > 1) {
                    log.warn("coverage for $b did not have an exact match and $covsSize matches based on class name and method name -> chose first $key: ${covs.keys}")
                }
                transformCoverageUnitResults(b, value)
            }
            .getOrNull()
    }

    private fun callsByGroup(b: Benchmark): Coverage? {
        // find the coverages that matches the group name
        val covs = coverages.coverages.filterKeys {
            val covBench = it as Benchmark
            covBench.clazz == b.clazz && covBench.group == b.name
        }

        // no coverages found for benchmark group
        if (covs.isEmpty()) {
            return null
        }

        // create group benchmark
        val groupBenchmark = MF.benchmark(
                clazz = b.clazz,
                name = b.name,
                params = b.params,
                jmhParams = b.jmhParams
        )

        return covs
                .map { rs ->
                    transformCoverageUnitResults(groupBenchmark, rs.value)
                }
                .fold(Coverage(of = groupBenchmark, unitResults = setOf())) { acc, rs ->
                    acc.union(rs)
                }
    }

    private fun transformCoverageUnitResults(b: Benchmark, cov: Coverage): Coverage =
            Coverage(
                    of = b,
                    unitResults = cov.all(true).map { cur ->
                        when (cur) {
                            is NotCovered -> CUF.notCovered(of = b, unit = cur.unit)
                            is PossiblyCovered -> CUF.possiblyCovered(of = b, unit = cur.unit, level = cur.level, probability = cur.probability)
                            is Covered -> CUF.covered(of = b, unit = cur.unit, level = cur.level)
//                            is PartiallyCovered -> CUF.partiallyCovered()
                        }
                    }.toSet()
            )

    companion object {
        val log = LogManager.getLogger(GreedyPrioritizer::class.java.canonicalName)
    }
}
