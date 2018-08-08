package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import org.funktionale.either.Either

class TotalPrioritizer(
        cgResult: CGResult,
        methodWeights: MethodWeights
) : GreedyPrioritizer(cgResult, methodWeights) {

    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        val prioritizedMethods = benchs.map { benchValue(it, setOf()) }.map { it.first }

        val orderedBenchs = prioritizedMethods
                .sortedWith(compareByDescending { it.priority.value })

        return Either.right(rankBenchs(orderedBenchs))
    }
}
