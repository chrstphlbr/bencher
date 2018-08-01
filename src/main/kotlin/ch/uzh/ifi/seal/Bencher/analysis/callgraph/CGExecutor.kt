package ch.uzh.ifi.seal.bencher.analysis.callgraph

import org.funktionale.either.Either
import java.io.File

interface CGExecutor {
    // calculates a call graph for a given jar file
    // returns either an error (left) or a valid CGResult (right)
    fun get(jar: File): Either<String, CGResult>
}

class CachedCGExecutor(private val cgExecutor: CGExecutor) : CGExecutor {

    private val results: MutableMap<String, CGResult> = mutableMapOf()

    override fun get(jar: File): Either<String, CGResult> {
        val jarFilePath = jar.absolutePath
        val cachedRes = results[jarFilePath]
        if (cachedRes != null) {
            return Either.right(cachedRes)
        }

        val eCgRes = cgExecutor.get(jar)
        if (eCgRes.isLeft()) {
            return eCgRes
        }

        val cgRes = eCgRes.right().get()
        results[jarFilePath] = cgRes
        return Either.right(cgRes)
    }
}
