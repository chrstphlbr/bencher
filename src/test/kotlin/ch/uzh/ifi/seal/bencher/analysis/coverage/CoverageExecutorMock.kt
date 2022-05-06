package ch.uzh.ifi.seal.bencher.analysis.coverage

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.Coverage
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.PossiblyCovered
import java.nio.file.Path

class CoverageExecutorMock private constructor(private val cov: Coverages) : CoverageExecutor {
    override fun get(jar: Path): Either<String, Coverages> =
            Either.Right(cov)

    companion object {
        fun new(vararg cov: Pair<Method, Set<PossiblyCovered>>): CoverageExecutorMock =
                CoverageExecutorMock(
                        Coverages(
                                coverages = cov.associate {
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
