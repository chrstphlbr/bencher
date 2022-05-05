package ch.uzh.ifi.seal.bencher.analysis.callgraph

import arrow.core.Either
import java.nio.file.Path

interface CGExecutor {
    // calculates a call graph for a given jar file
    // returns either an error (left) or a valid CGResult (right)
    fun get(jar: Path): Either<String, Coverages>
}

class CachedCGExecutor(private val cgExecutor: CGExecutor) : CGExecutor {

    private val results: MutableMap<Path, Coverages> = mutableMapOf()

    override fun get(jar: Path): Either<String, Coverages> {
        val cachedRes = results[jar]
        if (cachedRes != null) {
            return Either.Right(cachedRes)
        }

        return cgExecutor.get(jar).map {
            results[jar] = it
            it
        }
    }
}
