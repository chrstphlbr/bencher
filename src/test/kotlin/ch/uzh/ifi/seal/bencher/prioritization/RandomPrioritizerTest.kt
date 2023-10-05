package ch.uzh.ifi.seal.bencher.prioritization

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RandomPrioritizerTest {
    private fun prioritizedBenchs(
        p: RandomPrioritizer,
        benchs: Iterable<Benchmark>
    ): List<PrioritizedMethod<Benchmark>> =
        p.prioritize(benchs).getOrElse {
            Assertions.fail<String>("Could not prioritize benchmarks: $it")
            throw IllegalStateException("should not happen")
        }

    @Test
    fun empty() {
        val p = RandomPrioritizer()
        val pbs = prioritizedBenchs(p, listOf())
        Assertions.assertEquals(0, pbs.size)
    }

    private fun randomOrderAssertations(pbs: List<PrioritizedMethod<Benchmark>>, exp: List<Benchmark>) {
        Assertions.assertEquals(exp.size, pbs.size)

        Assertions.assertEquals(4, pbs.size)

        val pb1 = pbs[0]
        Assertions.assertEquals(exp[0], pb1.method)
        Assertions.assertEquals(Priority(rank = 1, total = 4, value = PrioritySingle(4.0)), pb1.priority)

        val pb2 = pbs[1]
        Assertions.assertEquals(exp[1], pb2.method)
        Assertions.assertEquals(Priority(rank = 2, total = 4, value = PrioritySingle(3.0)), pb2.priority)

        val pb3 = pbs[2]
        Assertions.assertEquals(exp[2], pb3.method)
        Assertions.assertEquals(Priority(rank = 3, total = 4, value = PrioritySingle(2.0)), pb3.priority)

        val pb4 = pbs[3]
        Assertions.assertEquals(exp[3], pb4.method)
        Assertions.assertEquals(Priority(rank = 4, total = 4, value = PrioritySingle(1.0)), pb4.priority)
    }

    @Test
    fun prioritizeFirst() {
        val p = RandomPrioritizer { 0.0 }
        val benchs = listOf(
            JarTestHelper.BenchParameterized.bench1,
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.OtherBench.bench3,
            JarTestHelper.BenchParameterized2.bench4
        )
        val pbs = prioritizedBenchs(p, benchs)
        randomOrderAssertations(pbs, benchs)
    }

    @Test
    fun prioritizeMid() {
        val p = RandomPrioritizer { 0.5 }
        val benchs = listOf(
            JarTestHelper.BenchParameterized.bench1,
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.OtherBench.bench3,
            JarTestHelper.BenchParameterized2.bench4
        )
        val pbs = prioritizedBenchs(p, benchs)
        val exp = listOf(
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.OtherBench.bench3,
            JarTestHelper.BenchParameterized.bench1,
            JarTestHelper.BenchParameterized2.bench4
        )
        randomOrderAssertations(pbs, exp)
    }

    @Test
    fun prioritizeLast() {
        val p = RandomPrioritizer { 1.0 }
        val benchs = listOf(
            JarTestHelper.BenchParameterized.bench1,
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.OtherBench.bench3,
            JarTestHelper.BenchParameterized2.bench4
        )
        val pbs = prioritizedBenchs(p, benchs)
        randomOrderAssertations(pbs, benchs.asReversed())
    }
}