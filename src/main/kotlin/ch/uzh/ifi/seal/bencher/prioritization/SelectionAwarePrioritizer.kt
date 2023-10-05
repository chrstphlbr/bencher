package ch.uzh.ifi.seal.bencher.prioritization

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.selection.Selector

class SelectionAwarePrioritizer(
    private val selector: Selector,
    private val prioritizer: Prioritizer,
        // true:    do prioritization on all benchmarks and then categorize them into the two sets (selected and not-selected, e.g., by FullChangeSelector)
        // false:   categorize into two sets (as above) and then do prioritization for each set
    private val singlePrioritization: Boolean = true
) : Prioritizer {

    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> =
            if (singlePrioritization) {
                singlePrioritization(benchs)
            } else {
                selectionSetPrioritization(benchs)
            }

    private fun selectionSetPrioritization(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        val sbs = selector.select(benchs).getOrElse {
            return Either.Left(it)
        }

        val psbs = prioritizer.prioritize(sbs).getOrElse {
            return Either.Left(it)
        }

        val pnsbs = prioritizer
            .prioritize(benchs.filter { !sbs.contains(it) })
            .getOrElse {
                return Either.Left(it)
            }

        return Either.Right(concatPrios(psbs, pnsbs))
    }

    private fun singlePrioritization(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        val pbs = prioritizer.prioritize(benchs).getOrElse {
            return Either.Left(it)
        }

        val sbs = selector.select(benchs).getOrElse {
            return Either.Left(it)
        }

        return Companion.singlePrioritization(pbs, sbs)
    }

    companion object {

        fun singlePrioritization(
            prioritizedBenchmarks: List<PrioritizedMethod<Benchmark>>,
            selectedBenchmarks: Iterable<Benchmark>
        ): Either<String, List<PrioritizedMethod<Benchmark>>> {
            val psbs = prioritizedBenchmarks.filter { selectedBenchmarks.contains(it.method) }

            val pnsbs = prioritizedBenchmarks.filter { !selectedBenchmarks.contains(it.method) }

            return Either.Right(concatPrios(psbs, pnsbs))
        }

        private fun concatPrios(
            p1: List<PrioritizedMethod<Benchmark>>,
            p2: List<PrioritizedMethod<Benchmark>>
        ): List<PrioritizedMethod<Benchmark>> {
            // rerank benchmarks due to different possible strategies (selectionSetPrioritization, singlePrioritization)
            val p1Reranked = Prioritizer.rankBenchs(p1)
            val p2Reranked = Prioritizer.rankBenchs(p2)

            // update rank of p2
            val p1Size = p1.size
            val np = p1Reranked +
                    p2Reranked.map { b -> b.copy(priority = b.priority.copy(rank = b.priority.rank + p1Size)) }
            val totalSize = np.size
            // update total of all
            return np.map { b -> b.copy(priority = b.priority.copy(total = totalSize)) }
        }
    }
}
