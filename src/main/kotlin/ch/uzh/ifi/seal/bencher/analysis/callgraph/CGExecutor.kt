package ch.uzh.ifi.seal.bencher.analysis.callgraph

import arrow.core.Either
import java.nio.file.Path

interface CGExecutor {
    // calculates a call graph for a given jar file
    // returns either an error (left) or a valid CGResult (right)
    fun get(jar: Path): Either<String, CGResult>
}

class CachedCGExecutor(private val cgExecutor: CGExecutor) : CGExecutor {

    private val results: MutableMap<Path, CGResult> = mutableMapOf()

    override fun get(jar: Path): Either<String, CGResult> {
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
