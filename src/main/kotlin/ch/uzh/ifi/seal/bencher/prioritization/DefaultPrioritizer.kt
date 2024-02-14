package ch.uzh.ifi.seal.bencher.prioritization

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.JavaSettings
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import ch.uzh.ifi.seal.bencher.parameterizedBenchmarks
import org.apache.logging.log4j.LogManager
import java.nio.file.Path

class DefaultPrioritizer(
    private val jar: Path,
    private val javaSettings: JavaSettings,
) : Prioritizer {
    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        val bf = JarBenchFinder(jar = jar, javaSettings = javaSettings)
        val bs = bf.all()
            .getOrElse {
                return Either.Left(it)
            }
            .parameterizedBenchmarks()

        val selected = mutableSetOf<Benchmark>()
        val filtered = bs
            .mapNotNull { findExactMatch(it, benchs) ?: findPartialMatch(it, benchs) }
            .filter { keepBenchmark(it, selected) }

        return Either.Right(
            filtered.mapIndexed { i, b ->
                PrioritizedMethod(
                    method = b,
                    priority = Priority(
                        rank = i + 1,
                        total = filtered.size,
                        value = PrioritySingle((filtered.size - i).toDouble())
                    )
                )
            }
        )
    }

    private fun findExactMatch(b: Benchmark, benchs: Iterable<Benchmark>): Benchmark? = benchs.find { it == b }

    private fun findPartialMatch(b: Benchmark, benchs: Iterable<Benchmark>): Benchmark? {
        val filteredClassMethod = filterClassMethod(b, benchs)

        return when {
            // no benchmark found
            filteredClassMethod.isEmpty() -> {
                log.debug("No benchmark for $b found with matching class name and method name")
                null
            }

            // one benchmark found
            filteredClassMethod.size == 1 -> filteredClassMethod[0]

            // multiple benchmarks found -> should never have different parameters but only different JMH params
            else -> {
                val filteredJMHParams = filterJMHParams(b, filteredClassMethod)

                when {
                    // single match with class name, method name, and JMH parameters (only function params not matching)
                    filteredJMHParams.size == 1 -> filteredJMHParams[0]

                    // no benchmark found, should never happen
                    filteredJMHParams.isEmpty() -> {
                        log.error("No benchmark for $b found with matching class name, method name, and JMH parameters")
                        null
                    }

                    // ambiguous match, should never happen
                    else -> {
                        log.error("Ambiguous matches for benchmark $b with matching class name, method name, and JMH parameters: $filteredJMHParams")
                        null
                    }
                }
            }
        }
    }

    private fun filterClassMethod(b: Benchmark, benchs: Iterable<Benchmark>): List<Benchmark> =
            benchs.filter { b1 -> b.clazz == b1.clazz && b.name == b1.name }

    private fun filterJMHParams(b: Benchmark, benchs: Iterable<Benchmark>): List<Benchmark> =
            benchs.filter { b1 -> b.jmhParams == b1.jmhParams }

    private fun keepBenchmark(b: Benchmark, selected: MutableSet<Benchmark>): Boolean =
            if (selected.contains(b)) {
                false
            } else {
                selected.add(b)
                true
            }

    private fun hashSetWithoutParams(i: Iterable<Benchmark>): HashSet<Benchmark> =
            i.map { it.copy(params = listOf()) }.toHashSet()

    companion object {
        val log = LogManager.getLogger(DefaultPrioritizer::class.java.canonicalName)
    }
}
