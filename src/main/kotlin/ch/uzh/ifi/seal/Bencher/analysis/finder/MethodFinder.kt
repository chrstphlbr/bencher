package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import org.funktionale.either.Either

interface MethodFinder<out T : Method> {
    fun all(): Either<String, List<T>>
}

interface BenchmarkFinder : MethodFinder<Benchmark> {
    fun setups(b: Benchmark): Collection<SetupMethod>
    fun tearDowns(b: Benchmark): Collection<TearDownMethod>
}
