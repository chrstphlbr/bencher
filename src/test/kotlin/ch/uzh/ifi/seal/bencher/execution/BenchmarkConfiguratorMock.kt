package ch.uzh.ifi.seal.bencher.execution

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark

class BenchmarkConfiguratorMock(private val bcs: Map<Benchmark, ExecutionConfiguration>) : BenchmarkConfigurator {
    override fun config(bench: Benchmark): Either<String, ExecutionConfiguration> =
            bcs[bench].let { c ->
                if (c == null) {
                    Either.Left("No configuration for benchmark $bench")
                } else {
                    Either.Right(c)
                }
            }
}
