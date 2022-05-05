package ch.uzh.ifi.seal.bencher.analysis.callgraph

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.PossiblyCovered
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.Coverage
import java.nio.file.Path

class CGExecutorMock private constructor(private val cgRes: CGResult) : CGExecutor {
    override fun get(jar: Path): Either<String, CGResult> =
            Either.Right(cgRes)

    companion object {
        fun new(vararg cg: Pair<Method, Set<PossiblyCovered>>): CGExecutorMock =
                CGExecutorMock(
                        CGResult(
                                calls = cg.associate {
                                    Pair(
                                        it.first,
                                        Coverage(
                                            of = it.first,
                                            unitResults = it.second
                                        )
                                    )
                                }
                        )
                )
    }
}
