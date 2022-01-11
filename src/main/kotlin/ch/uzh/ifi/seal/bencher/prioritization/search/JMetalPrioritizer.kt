package ch.uzh.ifi.seal.bencher.prioritization.search

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import ch.uzh.ifi.seal.bencher.prioritization.Prioritizer

class JMetalPrioritizer(
    private val cgResult: CGResult,
    private val methodWeights: MethodWeights,
) : Prioritizer {

    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        TODO("Not yet implemented")
    }
}
