package ch.uzh.ifi.seal.bencher.analysis.callgraph

import org.funktionale.either.Either

interface CGExecutor {
    fun get(): Either<CGResult, String>
}