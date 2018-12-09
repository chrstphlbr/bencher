package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Method
import org.funktionale.either.Either
import java.nio.file.Path

class CGExecutorMock private constructor(private val cgRes: CGResult) : CGExecutor {
    override fun get(jar: Path): Either<String, CGResult> =
            Either.right(cgRes)

    companion object {
        fun new(vararg cg: Pair<Method, List<MethodCall>>): CGExecutorMock =
                CGExecutorMock(
                    CGResult(
                            calls = mapOf(*cg)
                    )
                )
    }
}
