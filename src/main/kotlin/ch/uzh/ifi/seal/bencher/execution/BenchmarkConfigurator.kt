package ch.uzh.ifi.seal.bencher.execution

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark

interface BenchmarkConfigurator {
    fun config(bench: Benchmark): Either<String, ExecutionConfiguration>

    fun configs(benchs: Iterable<Benchmark>): Map<Benchmark, Either<String, ExecutionConfiguration>> =
        benchs.associateWith { b -> config(b) }
}
