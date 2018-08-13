package ch.uzh.ifi.seal.bencher.execution

import ch.uzh.ifi.seal.bencher.Benchmark
import org.funktionale.either.Either

class BenchmarkConfiguratorMock(private val bcs: Map<Benchmark, ExecutionConfiguration>) : BenchmarkConfigurator {
    override fun config(bench: Benchmark): Either<String, ExecutionConfiguration> =
            bcs[bench].let { c ->
                if (c == null) {
                    Either.left("No configuration for benchmark $bench")
                } else {
                    Either.right(c)
                }
            }
}
