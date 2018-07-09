package ch.uzh.ifi.seal.bencher.analysis

import ch.uzh.ifi.seal.bencher.Benchmark
import org.funktionale.either.Either

interface BenchmarkFinder {
    fun all(): Either<List<Benchmark>, String>
}