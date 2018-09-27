package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.MethodCall
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights

abstract class GreedyPrioritizer(
        private val cgResult: CGResult,
        private val methodWeights: MethodWeights
): Prioritizer {

    protected fun benchValue(b: Benchmark, alreadySelected: Set<Method>): Pair<PrioritizedMethod<Benchmark>, Set<Method>> {
        val bcs = cgResult.benchCalls[b]
        val p = if (bcs == null) {
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
            val (v, s) = benchCallSum(bcs, alreadySelected)
            Pair(
                    PrioritizedMethod(
                            method = b,
                            priority = Priority(
                                    rank = 0,
                                    total = 0,
                                    value = v
                            )
                    ),
                    alreadySelected + s
            )
        }

        return p
    }

    private fun benchCallSum(bcs: Iterable<MethodCall>, alreadySelected: Set<Method>): Pair<Double, Set<Method>> =
            benchCallSum(bcs.toList(), alreadySelected, 0.0)

    private tailrec fun benchCallSum(bcs: List<MethodCall>, alreadySelected: Set<Method>, currentSum: Double): Pair<Double, Set<Method>> =
            if (bcs.isEmpty()) {
                Pair(currentSum, alreadySelected)
            } else {
                val mc = bcs[0]
                val m = mc.method
                if (!alreadySelected.contains(m)) {
                    val pm = PlainMethod(
                            clazz = m.clazz,
                            name = m.name,
                            params = m.params
                    )
                    val weight: Double = methodWeights[pm] ?: 0.0

                    benchCallSum(bcs.drop(1), alreadySelected + m, currentSum + weight)
                } else {
                    // already added prio of this method (method can be contained multiple times because of multiple levels in CG)
                    benchCallSum(bcs.drop(1), alreadySelected, currentSum)
                }
            }
}
