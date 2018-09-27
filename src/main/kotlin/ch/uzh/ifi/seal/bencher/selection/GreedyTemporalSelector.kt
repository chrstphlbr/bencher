package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.execution.ExecTimePredictor
import org.apache.logging.log4j.LogManager
import org.funktionale.either.Either
import java.time.Duration

class GreedyTemporalSelector(
        private val budget: Duration,
        private val timePredictor: ExecTimePredictor
) : Selector {

    override fun select(benchs: Iterable<Benchmark>): Either<String, Iterable<Benchmark>> {
        val times = timePredictor.execTimes(benchs)

        var currentBudget = budget
        log.info("Select benchmarks (greedily) for budget ${budget.seconds}s")

        var totalBenchs = 0
        var selectedBenchs = 0

        val sel = benchs.filter { b ->
            totalBenchs++
            val t = times[b]

            if (t == null) {
                return Either.left("No time prediction for bench ($b)")
            } else if (t.isLeft()) {
                return Either.left("Invalid time prediction for bench ($b): ${t.left().get()}")
            } else {
                val newBudget = currentBudget.minus(t.right().get())
                if (newBudget.isNegative) {
                    false
                } else {
                    currentBudget = newBudget
                    selectedBenchs++
                    true
                }
            }
        }

        log.info("Selected $selectedBenchs/$totalBenchs benchmarks with remaining budget ${currentBudget.seconds}s")

        return Either.right(sel)
    }

    companion object {
        private val log = LogManager.getLogger(GreedyTemporalSelector::class.java)
    }
}
