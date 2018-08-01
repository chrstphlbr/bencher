package ch.uzh.ifi.seal.bencher.analysis.callgraph

import org.funktionale.either.Either

interface CGExecutor {
    // calculates a call graph for a given jar file
    // returns either an error (left) or a valid CGResult
    fun get(jar: String): Either<String, CGResult>
}
