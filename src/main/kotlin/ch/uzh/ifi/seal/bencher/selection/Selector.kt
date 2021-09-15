package ch.uzh.ifi.seal.bencher.selection

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark

interface Selector {
    // select takes an iterable of benchmarks `benchs`
    // and returns a (non-strict) subset of these (Either.right)
    // depending on the selection strategy employed by the concrete class
    // select is order-preserving (if the iterable has an order, the resulting selection has the same order)
    // if something went wrong during selection, an error is returned (Either.left)
    fun select(benchs: Iterable<Benchmark>): Either<String, Iterable<Benchmark>>
}
