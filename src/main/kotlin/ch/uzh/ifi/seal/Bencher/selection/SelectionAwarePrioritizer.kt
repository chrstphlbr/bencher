package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import org.funktionale.either.Either

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
        val sbs = selector.select(benchs)

        val ePsbs = prioritizer.prioritize(sbs)
        if (ePsbs.isLeft()) {
            return Either.left(ePsbs.left().get())
        }
        val psbs = ePsbs.right().get()

        val ePnsbs = prioritizer.prioritize(benchs.filter { !sbs.contains(it) })
        if (ePnsbs.isLeft()) {
            return Either.left(ePnsbs.left().get())
        }
        val pnsbs = ePnsbs.right().get()

        return Either.right(concatPrios(psbs, pnsbs))
    }

    private fun singlePrioritization(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        val ePbs = prioritizer.prioritize(benchs)
        if (ePbs.isLeft()) {
            return Either.left(ePbs.left().get())
        }

        val pbs = ePbs.right().get()
        val sbs = selector.select(benchs)

        val psbs = pbs.filter { sbs.contains(it.method) }

        val pnsbs = pbs.filter { !sbs.contains(it.method) }

        return Either.right(concatPrios(psbs, pnsbs))
    }

    private fun concatPrios(p1: List<PrioritizedMethod<Benchmark>>, p2: List<PrioritizedMethod<Benchmark>>): List<PrioritizedMethod<Benchmark>> {
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
