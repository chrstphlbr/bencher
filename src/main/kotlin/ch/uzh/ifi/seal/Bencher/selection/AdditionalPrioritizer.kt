package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutor
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeighter
import org.funktionale.either.Either
import java.nio.file.Path

class AdditionalPrioritizer(
        cgExecutor: CGExecutor,
        jarFile: Path,
        methodWeighter: MethodWeighter
) : GreedyPrioritizer(cgExecutor, jarFile, methodWeighter) {

    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        val o = prePrioritize()
        if (o.isDefined()) {
            return Either.left(o.get())
        }

        val prioritizedBenchs = prioritizeBenchs(benchs, false)
        return Either.right(prioritizedBenchs)
    }
}
