package ch.uzh.ifi.seal.bencher.selection

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.execution.ExecTimePredictorMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.time.Duration

class GreedyTemporalSelectorTest {

    private fun assertSelectedBenchs(e: Either<String, Iterable<Benchmark>>): List<Benchmark> =
        e.getOrElse {
            Assertions.fail<String>("Could not select benchmarks: $it")
            throw IllegalStateException("should not happen")
        }.toList()

    @Test
    fun errorNoPrediction() {
        val d = Duration.ofMinutes(5)
        val p = ExecTimePredictorMock(mapOf())

        val s = GreedyTemporalSelector(
            budget = d,
            timePredictor = p
        )

        val shouldBeError = s.select(fullBenchList)
        Assertions.assertTrue(shouldBeError.isLeft())
    }

    @Test
    fun selectNoneNoBenchmarks() {
        val d = Duration.ofMinutes(5)

        val p = ExecTimePredictorMock(
            mapOf(
                Pair(b1, Duration.ofSeconds(30)),
                Pair(b2, Duration.ofSeconds(60)),
                Pair(b3, Duration.ofSeconds(60)),
                Pair(b4, Duration.ofSeconds(60)),
                Pair(b5, Duration.ofSeconds(30))
            )
        )


        val s = GreedyTemporalSelector(
            budget = d,
            timePredictor = p
        )

        val benchs = listOf<Benchmark>()
        val eSelectedBenchs = s.select(benchs)
        val selectedBenchs = assertSelectedBenchs(eSelectedBenchs)

        Assertions.assertTrue(selectedBenchs.isEmpty())
    }

    @Test
    fun selectNoneTemporalReasons() {
        val d = Duration.ofSeconds(20)

        val p = ExecTimePredictorMock(
            mapOf(
                Pair(b1, Duration.ofSeconds(30)),
                Pair(b2, Duration.ofSeconds(60)),
                Pair(b3, Duration.ofSeconds(60)),
                Pair(b4, Duration.ofSeconds(60)),
                Pair(b5, Duration.ofSeconds(30))
            )
        )


        val s = GreedyTemporalSelector(
            budget = d,
            timePredictor = p
        )

        val benchs = fullBenchList
        val eSelectedBenchs = s.select(benchs)
        val selectedBenchs = assertSelectedBenchs(eSelectedBenchs)
        Assertions.assertTrue(selectedBenchs.isEmpty())
    }

    // repeat test 5 times because of random-order input (shuffled)
    @RepeatedTest(5)
    fun selectAll() {
        val d = Duration.ofSeconds(180)

        val p = ExecTimePredictorMock(
            mapOf(
                Pair(b1, Duration.ofSeconds(30)),
                Pair(b2, Duration.ofSeconds(30)),
                Pair(b3, Duration.ofSeconds(30)),
                Pair(b4, Duration.ofSeconds(30)),
                Pair(b5, Duration.ofSeconds(30))
            )
        )


        val s = GreedyTemporalSelector(
            budget = d,
            timePredictor = p
        )

        val benchs = fullBenchList.shuffled()
        Assertions.assertTrue(benchs.size == fullBenchList.size)

        val eSelectedBenchs = s.select(benchs)
        val selectedBenchs = assertSelectedBenchs(eSelectedBenchs)

        val size = selectedBenchs.size
        val expSize = 5

        Assertions.assertTrue(size == expSize, "Invalid size of selected benchs. Expected $expSize, was $size")

        // selected elements
        (0 until 5).forEach { i ->
            val sb = selectedBenchs[i]
            Assertions.assertTrue(sb == benchs[i])
        }
    }

    // repeat test 5 times because of random-order input (shuffled)
    @RepeatedTest(5)
    fun selectFirstThree() {
        val d = Duration.ofMinutes(2)

        val p = ExecTimePredictorMock(
            mapOf(
                Pair(b1, Duration.ofSeconds(40)),
                Pair(b2, Duration.ofSeconds(40)),
                Pair(b3, Duration.ofSeconds(40)),
                Pair(b4, Duration.ofSeconds(40)),
                Pair(b5, Duration.ofSeconds(40))
            )
        )


        val s = GreedyTemporalSelector(
            budget = d,
            timePredictor = p
        )

        val benchs = fullBenchList.shuffled()
        Assertions.assertTrue(benchs.size == fullBenchList.size)
        val eSelectedBenchs = s.select(benchs)
        val selectedBenchs = assertSelectedBenchs(eSelectedBenchs)

        val size = selectedBenchs.size
        val expSize = 3

        Assertions.assertTrue(size == expSize, "Invalid size of selected benchs. Expected $expSize, was $size")

        // selected elements
        (0 until 3).forEach { i ->
            val sb = selectedBenchs[i]
            Assertions.assertTrue(sb == benchs[i])
        }
    }

    @Test
    fun selectSome() {
        val d = Duration.ofMinutes(2)

        val p = ExecTimePredictorMock(
            mapOf(
                Pair(b1, Duration.ofSeconds(30)),
                Pair(b2, Duration.ofSeconds(60)),
                Pair(b3, Duration.ofSeconds(60)),
                Pair(b4, Duration.ofSeconds(60)),
                Pair(b5, Duration.ofSeconds(30))
            )
        )


        val s = GreedyTemporalSelector(
            budget = d,
            timePredictor = p
        )

        val benchs = fullBenchList
        val eSelectedBenchs = s.select(benchs)
        val selectedBenchs = assertSelectedBenchs(eSelectedBenchs)

        val size = selectedBenchs.size
        val expSize = 3

        Assertions.assertTrue(size == expSize, "Invalid size of selected benchs. Expected $expSize, was $size")

        // selected elements
        val sb1 = selectedBenchs[0]
        Assertions.assertTrue(sb1 == b1)

        val sb2 = selectedBenchs[1]
        Assertions.assertTrue(sb2 == b2)

        val sb3 = selectedBenchs[2]
        Assertions.assertTrue(sb3 == b5)
    }

    companion object {
        private val b1 = JarTestHelper.BenchParameterized.bench1
        private val b2 = JarTestHelper.BenchNonParameterized.bench2
        private val b3 = JarTestHelper.OtherBench.bench3
        private val b4 = JarTestHelper.BenchParameterized2.bench4
        private val b5 = JarTestHelper.NestedBenchmark.bench2

        private val fullBenchList = listOf(b1, b2, b3, b4, b5)
    }
}
