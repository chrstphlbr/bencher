package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Method
import org.funktionale.either.Either

interface MethodFinder<out T : Method> {
    fun all(): Either<String, List<T>>
}
