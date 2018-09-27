package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import org.funktionale.either.Either

interface Prioritizer {
    // takes an Iterable of benchmarks and returns a prioritized list sorted of these methods by their priority (descending)
    // might not include benchmarks if they are not relevant anymore (e.g., were removed according to static analysis, etc)
    fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>>

    companion object {
        fun rankBenchs(benchs: List<PrioritizedMethod<Benchmark>>): List<PrioritizedMethod<Benchmark>> {
            // filter benchmarks that have no priority rank
            val filteredBenchs = benchs.filter { !(it.priority.rank == -1 && it.priority.total == -1) }

            val s = filteredBenchs.size
            var lastValue = 0.0
            var lastRank = 1
            return filteredBenchs
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
    }
}
