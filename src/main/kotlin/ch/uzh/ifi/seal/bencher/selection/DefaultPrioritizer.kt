package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import ch.uzh.ifi.seal.bencher.parameterizedBenchmarks
import org.funktionale.either.Either
import java.nio.file.Path

class DefaultPrioritizer(private val jar: Path) : Prioritizer {
    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        val bf = JarBenchFinder(jar = jar)
        val ebs = bf.all()
        if (ebs.isLeft()) {
            return Either.left(ebs.left().get())
        }
        val bs = ebs.right().get().parameterizedBenchmarks()

        val selected = mutableSetOf<Benchmark>()
        val filtered = bs.mapNotNull { b ->
            // try to find exact match
            val foundExact = benchs.find { it == b }
            if (foundExact != null) {
                foundExact
            } else {
                // find match based on class name and method name
                val found = benchs.find { b1 ->
                    //                val p = b1.jmhParams.map { b.jmhParams.contains(it) }.fold(true) { acc, b -> acc && b }
                    // JMH benchmarks are uniquely identified by their class and name (parameters and JMH parameters are irrelevant)
                    b.clazz == b1.clazz && b.name == b1.name
                }
                found
            }
        }.filter { b ->
            // remove duplicates
            if (selected.contains(b)) {
                false
            } else {
                selected.add(b)
                true
            }
        }


        return Either.right(
            filtered.mapIndexed { i, b ->
                PrioritizedMethod(
                    method = b,
                    priority = Priority(
                            rank = i+1,
                            total = filtered.size,
                            value = (filtered.size - i).toDouble()
                    )
                )
            }
        )
    }

    private fun hashSetWithoutParams(i: Iterable<Benchmark>): HashSet<Benchmark> =
            i.map { it.copy(params = listOf()) }.toHashSet()
}
