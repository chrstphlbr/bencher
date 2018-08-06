package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import org.funktionale.either.Either

interface Prioritizer {
    // takes an Iterable of benchmarks and returns a prioritized list sorted of these methods by their priority (descending)
    // might not include benchmarks if they are not relevant anymore (e.g., were removed according to static analysis, etc)
    fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>>
}
