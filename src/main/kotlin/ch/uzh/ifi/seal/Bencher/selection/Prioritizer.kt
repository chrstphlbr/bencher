package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import org.funktionale.either.Either

interface Prioritizer {
    // takes an Iterable of benchmarks and returns a prioritized list sorted of these methods by their priority (descending)
    fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>>
}
