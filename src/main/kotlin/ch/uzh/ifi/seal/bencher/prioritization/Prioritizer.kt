package ch.uzh.ifi.seal.bencher.prioritization

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import kotlin.random.Random

interface Prioritizer {
    // takes an Iterable of benchmarks and returns a prioritized list of these methods sorted by their priority (descending)
    // might not include benchmarks if they are not relevant anymore (e.g., were removed according to static analysis, etc)
    fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>>
    
    companion object {
        fun benchWithNoPrio(pb: PrioritizedMethod<Benchmark>): Boolean =
                pb.priority.rank == -1 && pb.priority.total == -1

        fun rankBenchs(benchs: Collection<PrioritizedMethod<Benchmark>>): List<PrioritizedMethod<Benchmark>> {
            // remove not prioritized benchmarks
            val filtered = benchs.filter { !benchWithNoPrio(it) }

            val s = filtered.size
            var lastValue = 0.0
            var lastRank = 1
            return filtered.mapIndexed { i, b ->
                val v: Double = when (val v = b.priority.value) {
                    is PrioritySingle -> v.value
                    // sum up all prioritiy values to get a single value
                    is PriorityMultiple -> v.values.sum()
                }

                val rank = if (lastValue == v) {
                    lastRank
                } else {
                    i + 1
                }

                lastRank = rank
                lastValue = v

                PrioritizedMethod(
                        method = b.method,
                        priority = Priority(rank = rank, total = s, value = b.priority.value)
                )
            }
        }
    }
}

interface PrioritizerMultipleSolutions : Prioritizer {
    // random is used internally to decide which solution to pick in prioritize
    val random: Random

    // takes an Iterable of benchmarks and returns a prioritized list of these methods sorted by their priority (descending)
    // might not include benchmarks if they are not relevant anymore (e.g., were removed according to static analysis, etc)
    // provides a default implementation of Prioritizer.prioritize that works when multiple solutions are available:
    // if there are multiple prioritized solutions (e.g., acquired through a multi-objective optimization algorithm), a randomly-selected best solution is returned
    // use prioritizeMultipleSolutions to get all solutions
    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> =
        prioritizeMultipleSolutions(benchs).map { solutions ->
            val idx = random.nextInt().mod(solutions.size)
            solutions[idx]
        }

    // prioritizeMultipleSolutions returns all prioritization solution for the provided benchmark Iterable
    fun prioritizeMultipleSolutions(benchs: Iterable<Benchmark>): Either<String, List<List<PrioritizedMethod<Benchmark>>>>
}
