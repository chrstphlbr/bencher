package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import org.apache.logging.log4j.LogManager
import org.funktionale.either.Either
import java.time.Duration
import java.time.LocalDateTime

class AdditionalPrioritizer(
        cgResult: CGResult,
        methodWeights: MethodWeights
) : GreedyPrioritizer(cgResult, methodWeights) {

    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        val bl = benchs.toList()
        log.info("Start prioritizing ${bl.size} benchmarks")
        val start = LocalDateTime.now()
        val pbs = prioritize(bl, setOf(), listOf())
        val end = LocalDateTime.now()
        log.info("Finished prioritizing in ${Duration.between(start, end)}")
        return Either.right(Prioritizer.rankBenchs(pbs))
    }

    private tailrec fun prioritize(benchs: List<Benchmark>, alreadySelected: Set<Method>, prioritizedBenchs: List<PrioritizedMethod<Benchmark>>): List<PrioritizedMethod<Benchmark>> =
            if (benchs.isEmpty()) {
                prioritizedBenchs
            } else {
                val start = LocalDateTime.now()
                val hb = benchs.map { benchValue(it, alreadySelected) }.maxWith(compareBy { it.first.priority.value })
                val end = LocalDateTime.now()
                log.info("Highest prio benchmark in ${Duration.between(start, end)}")
                if (hb == null) {
                    log.warn("Did not get a highest priority benchmark")
                    prioritizedBenchs
                } else {
                    prioritize(
                            benchs = benchs.filter { it != hb.first.method },
                            alreadySelected = alreadySelected + hb.second,
                            prioritizedBenchs = prioritizedBenchs + hb.first
                    )
                }
            }

    companion object {
        private val log = LogManager.getLogger(AdditionalPrioritizer::class.java.canonicalName)
    }
}
