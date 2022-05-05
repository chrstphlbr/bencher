package ch.uzh.ifi.seal.bencher.analysis.callgraph

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.Coverage
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.PossiblyCovered
import java.nio.file.Path

class CGExecutorMock private constructor(private val cgRes: Coverages) : CGExecutor {
    override fun get(jar: Path): Either<String, Coverages> =
            Either.Right(cgRes)

    companion object {
        fun new(vararg cg: Pair<Method, Set<PossiblyCovered>>): CGExecutorMock =
                CGExecutorMock(
                        Coverages(
                                coverages = cg.associate {
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
