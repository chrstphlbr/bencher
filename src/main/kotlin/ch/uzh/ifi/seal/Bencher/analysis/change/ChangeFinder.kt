package ch.uzh.ifi.seal.bencher.analysis.change

import org.funktionale.either.Either

interface ChangeFinder {
    fun changes(oldJar: String, newJar: String): Either<String, Set<Change>>
}
