package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.MethodCall
import org.funktionale.either.Either
import java.io.InputStream
import java.nio.file.Path

abstract class ExternalPrioritizer(
        protected val file: InputStream,
        private val callGraphExec: CGExecutor,
        private val jarFile: Path
) : Prioritizer {

    private var read: Boolean = false
    private lateinit var cgRes: CGResult
    private lateinit var priorities: Map<out Method, Priority>

    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        if (!read) {
            val ePrios = readPriorities()
            if (ePrios.isLeft()) {
                return Either.left(ePrios.left().get())
            }
            priorities = ePrios.right().get()

            val eCgRes = callGraphExec.get(jarFile)
            if (eCgRes.isLeft()) {
                return Either.left(eCgRes.left().get())
            }
            cgRes = eCgRes.right().get()

            read = true
        }

        val orderedBenchs = benchs.mapNotNull { benchValue(it) }.sortedWith(compareByDescending { it.priority.value })
        val s = orderedBenchs.size
        var lastValue = 0.0
        var lastRank = 1
        val prioritizedBenchs = orderedBenchs.mapIndexed { i, b ->
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
        return Either.right(prioritizedBenchs)
    }

    private fun benchValue(bench: Benchmark): PrioritizedMethod<Benchmark>? {
        val benchCalls = cgRes.benchCalls[bench] ?: return null
        return PrioritizedMethod(
                method = bench,
                priority = Priority(
                        rank = 0,
                        total = 0,
                        value = benchCallSum(benchCalls)
                )
        )
    }

    private fun benchCallSum(bcs: Iterable<MethodCall>): Double =
            benchCallSum(bcs.toList(), setOf(), 0.0)

    private tailrec fun benchCallSum(bcs: List<MethodCall>, alreadyVisited: Set<Method>, currentSum: Double): Double =
            if (bcs.isEmpty()) {
                currentSum
            } else {
                val mc = bcs[0]
                val m = mc.method
                if (!alreadyVisited.contains(m)) {
                    val pm = PlainMethod(
                            clazz = m.clazz,
                            name = m.name,
                            params = m.params
                    )
                    val prio = priorities[pm]
                    val p = if (prio != null) {
                        prio.value
                    } else {
                        0.0
                    }

                    benchCallSum(bcs.drop(1), alreadyVisited + m, currentSum + p)
                } else {
                    // already added prio of this method (method can be contained multiple times because of multiple levels in CG)
                    benchCallSum(bcs.drop(1), alreadyVisited, currentSum)
                }

            }

    protected abstract fun readPriorities(): Either<String, Map<out Method, Priority>>
}
