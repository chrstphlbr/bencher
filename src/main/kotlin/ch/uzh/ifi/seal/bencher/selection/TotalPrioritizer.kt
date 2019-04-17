package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import org.apache.logging.log4j.LogManager
import org.funktionale.either.Either
import java.time.Duration
import java.time.LocalDateTime

class TotalPrioritizer(
        cgResult: CGResult,
        methodWeights: MethodWeights
) : GreedyPrioritizer(cgResult, methodWeights) {

    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        val bl = benchs.toList()
        log.info("Start prioritizing ${bl.size} benchmarks")
        val start = LocalDateTime.now()
        val prioritizedMethods = benchs.map { benchValue(it, setOf()) }.map { it.first }

        val orderedBenchs = prioritizedMethods
                .sortedWith(compareByDescending { it.priority.value })

        val end = LocalDateTime.now()
        log.info("Finished prioritizing in ${Duration.between(start, end)}")

        return Either.right(Prioritizer.rankBenchs(orderedBenchs))
    }

    companion object {
        private val log = LogManager.getLogger(TotalPrioritizer::class.java.canonicalName)
    }
}
