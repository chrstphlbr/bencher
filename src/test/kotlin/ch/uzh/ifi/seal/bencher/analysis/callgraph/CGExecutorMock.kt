package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.PossiblyReachable
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Reachabilities
import org.funktionale.either.Either
import java.nio.file.Path

class CGExecutorMock private constructor(private val cgRes: CGResult) : CGExecutor {
    override fun get(jar: Path): Either<String, CGResult> =
            Either.right(cgRes)

    companion object {
        fun new(vararg cg: Pair<Method, Set<PossiblyReachable>>): CGExecutorMock =
                CGExecutorMock(
                    CGResult(
                            calls = cg.map {
                                Pair(
                                        it.first,
                                        Reachabilities(
                                                start = it.first,
                                                reachabilities = it.second
                                        )
                                )
                            }.toMap()
                    )
                )
    }
}
