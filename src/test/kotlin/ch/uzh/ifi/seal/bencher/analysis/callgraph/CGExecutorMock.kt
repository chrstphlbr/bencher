package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Benchmark
import org.funktionale.either.Either
import java.nio.file.Path

class CGExecutorMock private constructor(private val cgRes: CGResult) : CGExecutor {
    override fun get(jar: Path): Either<String, CGResult> =
            Either.right(cgRes)

    companion object {
        fun new(vararg cg: Pair<Benchmark, List<MethodCall>>): CGExecutorMock =
                CGExecutorMock(
                    CGResult(
                            benchCalls = mapOf(*cg)
                    )
                )
    }
}
