package ch.uzh.ifi.seal.bencher.selection

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark

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
                val v = b.priority.value
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
