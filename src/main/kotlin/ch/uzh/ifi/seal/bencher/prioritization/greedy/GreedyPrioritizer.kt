package ch.uzh.ifi.seal.bencher.prioritization.greedy

import arrow.core.firstOrNone
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.MF
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.Coverages
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.*
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import ch.uzh.ifi.seal.bencher.analysis.weight.methodCallWeight
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import ch.uzh.ifi.seal.bencher.prioritization.Prioritizer
import ch.uzh.ifi.seal.bencher.prioritization.Priority
import ch.uzh.ifi.seal.bencher.prioritization.PrioritySingle
import org.apache.logging.log4j.LogManager

abstract class GreedyPrioritizer(
    private val coverages: Coverages,
    private val methodWeights: MethodWeights,
) : Prioritizer {

    protected fun benchValue(b: Benchmark, alreadySelected: Set<Method>): Pair<PrioritizedMethod<Benchmark>, Set<Method>> {
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
            val value = methodCallWeight(
                    method = b,
                    coverage = calls,
                    methodWeights = methodWeights,
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
        // find the CG result that matches class name and method name of the benchmark
        val cgrs = coverages.coverages.filterKeys {
            it.clazz == b.clazz && it.name == b.name
        }

        val cgrsSize = cgrs.size

        return cgrs.entries
            .firstOrNone()
            .map { (key, value) ->
                if (cgrsSize > 1) {
                    log.warn("cgResult for $b did not have an exact match and $cgrsSize matches based on class name and method name -> chose first $key: ${cgrs.keys}")
                }
                transformReachabilities(b, value)
            }
            .orNull()
    }

    private fun callsByGroup(b: Benchmark): Coverage? {
        // find the CG result that matches the group name
        val cgrs = coverages.coverages.filterKeys {
            val cgBench = it as Benchmark
            cgBench.clazz == b.clazz && cgBench.group == b.name
        }

        // no CG result found for benchmark group
        if (cgrs.isEmpty()) {
            return null
        }

        // create group benchmark
        val groupBenchmark = MF.benchmark(
                clazz = b.clazz,
                name = b.name,
                params = b.params,
                jmhParams = b.jmhParams
        )

        return cgrs
                .map { rs ->
                    transformReachabilities(groupBenchmark, rs.value)
                }
                .fold(Coverage(of = groupBenchmark, unitResults = setOf())) { acc, rs ->
                    acc.union(rs)
                }
    }

    private fun transformReachabilities(b: Benchmark, rs: Coverage): Coverage =
            Coverage(
                    of = b,
                    unitResults = rs.all(true).map { rr ->
                        when (rr) {
                            is NotCovered -> CUF.notCovered(of = b, unit = rr.unit)
                            is PossiblyCovered -> CUF.possiblyCovered(of = b, unit = rr.unit, level = rr.level, probability = rr.probability)
                            is Covered -> CUF.covered(of = b, unit = rr.unit, level = rr.level)
                        }
                    }.toSet()
            )

    companion object {
        val log = LogManager.getLogger(GreedyPrioritizer::class.java.canonicalName)
    }
}
