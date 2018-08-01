package ch.uzh.ifi.seal.bencher.analysis.change

import org.funktionale.either.Either
import java.io.File

interface ChangeFinder {
    fun changes(oldJar: File, newJar: File): Either<String, Set<Change>>
}
