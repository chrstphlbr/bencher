package ch.uzh.ifi.seal.bencher.analysis.callgraph

import org.funktionale.either.Either

interface CGExecutor<out T : CGResult> {
    fun get(): Either<String, T>
}
