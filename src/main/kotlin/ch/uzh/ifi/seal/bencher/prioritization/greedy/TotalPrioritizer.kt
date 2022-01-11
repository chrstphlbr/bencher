package ch.uzh.ifi.seal.bencher.prioritization.greedy

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import ch.uzh.ifi.seal.bencher.prioritization.Prioritizer
import org.apache.logging.log4j.LogManager

class TotalPrioritizer(
        cgResult: CGResult,
        methodWeights: MethodWeights
) : GreedyPrioritizer(cgResult, methodWeights) {

    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        val bl = benchs.toList()
        log.info("Start prioritizing ${bl.size} benchmarks")
        val start = System.nanoTime()

        val prioritizedMethods = benchs.asSequence()
                .map { benchValue(it, setOf()) }
                .map { it.first }
                .sortedWith(compareByDescending { it.priority.value })
                .toList()

        val dur = System.nanoTime() - start
        log.info("Finished prioritizing in ${dur}ns")

        return Either.Right(Prioritizer.rankBenchs(prioritizedMethods))
    }

    companion object {
        private val log = LogManager.getLogger(TotalPrioritizer::class.java.canonicalName)
    }
}
