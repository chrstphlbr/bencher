package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.*
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import ch.uzh.ifi.seal.bencher.analysis.weight.methodCallWeight
import org.apache.logging.log4j.LogManager
import org.funktionale.option.firstOption

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
        return if (exactCalls != null) {
            exactCalls
        } else {
            // find the CG result that matches class name and method name of the benchmark
            val cgrs = cgResult.calls.filterKeys {
                it.clazz == b.clazz && it.name == b.name
            }

            val cgrsSize = cgrs.size
            if (cgrsSize >= 1) {
                // must always have at least one element -> firstOption and ret.get below are safe
                val ret = cgrs.values.firstOption()
                if (cgrsSize > 1) {
                    log.warn("cgResult did not have an exact match and $cgrsSize matches based on class name and method name -> chose first $ret: $cgrs")
                }
                transformReachabilities(b, ret.get())
            } else {
                null
            }
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
