package ch.uzh.ifi.seal.bencher.prioritization

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import kotlin.math.floor

typealias randomFunc = () -> Double

class RandomPrioritizer(private val random: randomFunc = Math::random) : Prioritizer {
    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        if (!benchs.iterator().hasNext()) {
            // empty iterable
            return Either.Right(listOf())
        }

        val bs = mutableListOf<Benchmark>()
        val succ = bs.addAll(benchs)
        if (!succ) {
            return Either.Left("Could not add benchs to mutable copy")
        }
        val length = bs.size

        val out: List<PrioritizedMethod<Benchmark>> = (1..length).map { i ->
            val r = floor(random() * (bs.size - 1)).toInt()
            val b = bs.removeAt(r)
            PrioritizedMethod(
                    method = b,
                    priority = Priority(
                            rank = i,
                            total = length,
                            value = PrioritySingle((length - i + 1).toDouble())
                    )
            )
        }

        return Either.Right(out)
    }
}
