package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.MethodCall
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeighter
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import org.funktionale.option.Option
import java.nio.file.Path

abstract class GreedyPrioritizer(
        private val cgExecutor: CGExecutor,
        private val jarFile: Path,
        private val methodWeighter: MethodWeighter
): Prioritizer {

    private lateinit var methodWeights: MethodWeights
    private lateinit var cgRes: CGResult

    protected fun prePrioritize(): Option<String> {
        val ePrios = methodWeighter.weights()
        if (ePrios.isLeft()) {
            return Option.Some(ePrios.left().get())
        }
        methodWeights = ePrios.right().get()

        val eCgRes = cgExecutor.get(jarFile)
        if (eCgRes.isLeft()) {
            return Option.Some(eCgRes.left().get())
        }
        cgRes = eCgRes.right().get()

        return Option.empty()
    }

    protected fun prioritizeBenchs(benchs: Iterable<Benchmark>, total: Boolean = true): List<PrioritizedMethod<Benchmark>> {
        val (prioritizedMethods, _) = benchs.fold(Pair(listOf<PrioritizedMethod<Benchmark>>(), setOf<Method>())) { acc, b ->
            val (p, s) = benchValue(b, acc.second)
            Pair(
                    acc.first + p,
                    if (total) {
                        setOf()
                    } else {
                        acc.second + s
                    }
            )
        }

        val orderedBenchs = prioritizedMethods
                .sortedWith(compareByDescending { it.priority.value })
                .filter { !(it.priority.rank == -1 && it.priority.total == -1) }

        val s = orderedBenchs.size
        var lastValue = 0.0
        var lastRank = 1
        return orderedBenchs
                .mapIndexed { i, b ->
                    val v = b.priority.value
                    val rank = if (lastValue == v) {
                        lastRank
                    } else {
                        i+1
                    }

                    lastRank = rank
                    lastValue = v

                    PrioritizedMethod(
                            method = b.method,
                            priority = Priority(rank = rank, total = s, value = b.priority.value)
                    )
                }
    }

    private fun benchValue(b: Benchmark, alreadySelected: Set<Method> = setOf()): Pair<PrioritizedMethod<Benchmark>, Set<Method>> {
        val bcs = cgRes.benchCalls[b]
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
