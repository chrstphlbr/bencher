package ch.uzh.ifi.seal.bencher.analysis.coverage

import arrow.core.Either
import java.nio.file.Path

interface CoverageExecutor {
    // calculates the coverages for a given jar file
    // returns either an error (left) or valid Coverages (right)
    fun get(jar: Path): Either<String, Coverages>
}

class CachedCoverageExecutor(private val coverageExecutor: CoverageExecutor) : CoverageExecutor {

    private val results: MutableMap<Path, Coverages> = mutableMapOf()

    override fun get(jar: Path): Either<String, Coverages> {
        val cachedRes = results[jar]
        if (cachedRes != null) {
            return Either.Right(cachedRes)
        }

        return coverageExecutor.get(jar).map {
            results[jar] = it
            it
        }
    }
}
