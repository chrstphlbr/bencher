package ch.uzh.ifi.seal.bencher.prioritization.greedy

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import ch.uzh.ifi.seal.bencher.prioritization.Prioritizer
import org.apache.logging.log4j.LogManager

class AdditionalPrioritizer(
        cgResult: CGResult,
        methodWeights: MethodWeights
) : GreedyPrioritizer(cgResult, methodWeights) {

    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        val bl = benchs.toMutableList()
        log.info("Start prioritizing ${bl.size} benchmarks")
        val start = System.nanoTime()
        val pbs = prioritize(bl, mutableSetOf(), mutableListOf(), 1, bl.size)
        val dur = System.nanoTime() - start
        log.info("Finished prioritizing in ${dur}ns")
        return Either.Right(Prioritizer.rankBenchs(pbs))
    }

    private tailrec fun prioritize(benchs: MutableList<Benchmark>, alreadySelected: MutableSet<Method>, prioritizedBenchs: MutableList<PrioritizedMethod<Benchmark>>, i: Int, total: Int): List<PrioritizedMethod<Benchmark>> =
            if (benchs.isEmpty()) {
                prioritizedBenchs
            } else {
                val start = System.nanoTime()
                val found = highestBenchmark(benchs, alreadySelected, prioritizedBenchs)
                val dur = System.nanoTime() - start
                log.info("Highest prio benchmark in ${dur}ns")
                if (!found) {
                    log.warn("Did not get a highest priority benchmark")
                    prioritizedBenchs
                } else {
                    // recursive call
                    prioritize(
                            benchs = benchs,
                            alreadySelected = alreadySelected,
                            prioritizedBenchs = prioritizedBenchs,
                            i = i + 1,
                            total = total
                    )
                }
            }

    private fun highestBenchmark(benchs: MutableList<Benchmark>, alreadySelected: MutableSet<Method>, prioritizedBenchs: MutableList<PrioritizedMethod<Benchmark>>): Boolean {
        var highest: Double = -1.0
        var idx = -1
        lateinit var bench: PrioritizedMethod<Benchmark>
        lateinit var newSel: Set<Method>

        var i = 0
        for (b in benchs) {
            val (pb, ns) = benchValue(b, alreadySelected)

            if (pb.priority.value > highest) {
                highest = pb.priority.value
                idx = i
                bench = pb
                newSel = ns
            }

            i++
        }

        return if (highest != -1.0 && idx >= 0) {
            benchs.removeAt(idx)
            alreadySelected.addAll(newSel)
            prioritizedBenchs.add(bench)
            true
        } else {
            false
        }
    }

    companion object {
        private val log = LogManager.getLogger(AdditionalPrioritizer::class.java.canonicalName)
    }
}
