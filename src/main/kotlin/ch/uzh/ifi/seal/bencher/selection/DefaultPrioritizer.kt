package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import org.funktionale.either.Either
import java.nio.file.Path

class DefaultPrioritizer(private val jar: Path) : Prioritizer {
    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        val bf = JarBenchFinder(jar = jar)
        val ebs = bf.all()
        if (ebs.isLeft()) {
            return Either.left(ebs.left().get())
        }
        val bs = ebs.right().get()

        // check if returned from JarBenchFinder WITHOUT function parameters,
        // as JMH enforces that fully-qualified benchmarks are unique,
        // hence no overloading is permitted
        // AND
        // results from JarBenchFinder never contain function parameters
        val filtered = bs.mapNotNull { b ->
            val found = benchs.find { b1 ->
                val p = b1.jmhParams.map { b.jmhParams.contains(it) }.fold(true) { acc, b -> acc && b }
                b.clazz == b1.clazz && b.name == b1.name && p
            }
            found
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
