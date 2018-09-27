package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration
import org.funktionale.either.Either

interface MethodFinder<out T : Method> {
    fun all(): Either<String, List<T>>
}

interface BenchmarkFinder : MethodFinder<Benchmark> {
    fun setups(b: Benchmark): Collection<SetupMethod>
    fun tearDowns(b: Benchmark): Collection<TearDownMethod>
    fun benchmarkExecutionInfos(): Either<String, Map<Benchmark, ExecutionConfiguration>>
    fun classExecutionInfos(): Either<String, Map<Class, ExecutionConfiguration>>
}
