package ch.uzh.ifi.seal.bencher.selection

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.execution.ExecTimePredictor
import org.apache.logging.log4j.LogManager
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
            val mt = times[b] ?: return Either.Left("No time prediction for bench ($b)")

            val t = mt.getOrElse {
                return Either.Left("Invalid time prediction for bench ($b): $it")
            }

            val newBudget = currentBudget.minus(t)
            if (newBudget.isNegative) {
                false
            } else {
                currentBudget = newBudget
                selectedBenchs++
                true
            }
        }

        log.info("Selected $selectedBenchs/$totalBenchs benchmarks with remaining budget ${currentBudget.seconds}s")

        return Either.Right(sel)
    }

    companion object {
        private val log = LogManager.getLogger(GreedyTemporalSelector::class.java)
    }
}
