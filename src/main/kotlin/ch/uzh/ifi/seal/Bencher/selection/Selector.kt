package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark

interface Selector {
    fun select(benchs: Iterable<Benchmark>): Iterable<Benchmark>
}
