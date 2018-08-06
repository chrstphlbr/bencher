package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.Method
import org.funktionale.either.Either

typealias MethodWeights = Map<out Method, Double>

interface MethodWeighter {
    fun weights(): Either<String, MethodWeights>
}