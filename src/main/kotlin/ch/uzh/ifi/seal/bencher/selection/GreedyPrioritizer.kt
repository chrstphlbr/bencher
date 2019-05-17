package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import ch.uzh.ifi.seal.bencher.analysis.weight.methodCallWeight

abstract class GreedyPrioritizer(
        private val cgResult: CGResult,
        methodWeights: MethodWeights,
        methodWeightMapper: MethodWeightMapper
): Prioritizer {

    private val mws = methodWeightMapper.map(methodWeights)

    protected fun benchValue(b: Benchmark, alreadySelected: Set<Method>): Pair<PrioritizedMethod<Benchmark>, Set<Method>> {
        val calls = cgResult.calls[b]
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
}
