package ch.uzh.ifi.seal.bencher.prioritization.greedy

import arrow.core.firstOrNone
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.MF
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.*
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import ch.uzh.ifi.seal.bencher.analysis.weight.methodCallWeight
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import ch.uzh.ifi.seal.bencher.prioritization.Prioritizer
import ch.uzh.ifi.seal.bencher.prioritization.Priority
import org.apache.logging.log4j.LogManager

abstract class GreedyPrioritizer(
        private val cgResult: CGResult,
        methodWeights: MethodWeights,
        methodWeightMapper: MethodWeightMapper
) : Prioritizer {

    private val mws = methodWeightMapper.map(methodWeights)

    protected fun benchValue(b: Benchmark, alreadySelected: Set<Method>): Pair<PrioritizedMethod<Benchmark>, Set<Method>> {
        val calls = calls(b)
        val p = if (calls == null) {
            Pair(
                    PrioritizedMethod(
                            method = b,
                            priority = Priority(
                                    rank = -1,
                                    total = -1,
                                    value = 0.0
                            )
                    ),
                    alreadySelected
            )
        } else {
            val value = methodCallWeight(
                    method = b,
                    reachability = calls,
                    methodWeights = mws,
                    exclusions = alreadySelected,
                    accumulator = Double::plus
            )

            Pair(
                    PrioritizedMethod(
                            method = b,
                            priority = Priority(
                                    rank = 0,
                                    total = 0,
                                    value = value.first
                            )
                    ),
                    value.second
            )
        }

        return p
    }

    private fun calls(b: Benchmark): Reachabilities? {
        val exactCalls = cgResult.calls[b]
        return exactCalls ?: callsByClassAndMethod(b) ?: callsByGroup(b)
    }

    private fun callsByClassAndMethod(b: Benchmark): Reachabilities? {
        // find the CG result that matches class name and method name of the benchmark
        val cgrs = cgResult.calls.filterKeys {
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

    private fun callsByGroup(b: Benchmark): Reachabilities? {
        // find the CG result that matches the group name
        val cgrs = cgResult.calls.filterKeys {
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
                .fold(Reachabilities(start = groupBenchmark, reachabilities = setOf())) { acc, rs ->
                    acc.union(rs)
                }
    }

    private fun transformReachabilities(b: Benchmark, rs: Reachabilities): Reachabilities =
            Reachabilities(
                    start = b,
                    reachabilities = rs.reachabilities(true).map { rr ->
                        when (rr) {
                            is NotReachable -> RF.notReachable(from = b, to = rr.to)
                            is PossiblyReachable -> RF.possiblyReachable(from = b, to = rr.to, level = rr.level, probability = rr.probability)
                            is Reachable -> RF.reachable(from = b, to = rr.to, level = rr.level)
                        }
                    }.toSet()
            )

    companion object {
        val log = LogManager.getLogger(GreedyPrioritizer::class.java.canonicalName)
    }
}
