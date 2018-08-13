package ch.uzh.ifi.seal.bencher.execution

import ch.uzh.ifi.seal.bencher.Benchmark
import org.funktionale.either.Either

interface BenchmarkConfigurator {
    fun config(bench: Benchmark): Either<String, ExecutionConfiguration>

    fun configs(benchs: Iterable<Benchmark>): Map<Benchmark, Either<String, ExecutionConfiguration>> =
            benchs.associate { b -> Pair(b, config(b)) }
}
