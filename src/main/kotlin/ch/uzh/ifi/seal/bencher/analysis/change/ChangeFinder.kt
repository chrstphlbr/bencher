package ch.uzh.ifi.seal.bencher.analysis.change

import arrow.core.Either
import java.io.File

interface ChangeFinder {
    fun changes(oldJar: File, newJar: File): Either<String, Set<Change>>
}
