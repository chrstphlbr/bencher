package ch.uzh.ifi.seal.bencher.prioritization.search

import arrow.core.getOrHandle
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BenchmarkIndexMapImplTest {

    @Test
    fun empty() {
        val bs = listOf<Benchmark>()
        val m = BenchmarkIndexMapImpl(bs)
        Assertions.assertEquals(0, m.size)
    }

    @Test
    fun oneBenchmarkSize() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIndexMapImpl(bs)

        Assertions.assertEquals(bs.size, m.size)
    }

    @Test
    fun oneBenchmarkGetBenchmark() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIndexMapImpl(bs)

        val idx = 0
        val returnedBenchmark = m[idx] ?: Assertions.fail("expected benchmark for index $idx")
        Assertions.assertEquals(b1, returnedBenchmark)
    }

    @Test
    fun oneBenchmarkGetBenchmarksEmpty() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIndexMapImpl(bs)

        val idxList = listOf<Int>()

        val returnedBenchmarks = m.benchmarks(idxList).getOrHandle {
            Assertions.fail("could not get benchmarks: $it")
        }

        Assertions.assertEquals(0, returnedBenchmarks.size)
    }

    @Test
    fun oneBenchmarkGetBenchmarksOne() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIndexMapImpl(bs)

        val idx = 0
        val idxList = listOf(idx)

        val returnedBenchmarks = m.benchmarks(idxList).getOrHandle {
            Assertions.fail("could not get benchmarks: $it")
        }

        Assertions.assertEquals(idxList.size, returnedBenchmarks.size)

        returnedBenchmarks.forEachIndexed { i, b ->
            val expectedIdx = idxList[i]
            val expectedBenchmark = m[expectedIdx] ?: Assertions.fail("no benchmark for index $expectedIdx, should be $b")
            Assertions.assertEquals(expectedBenchmark, b)
        }
    }

    @Test
    fun fourBenchmarkGetBenchmarksFour() {
        val bs = listOf(
            JarTestHelper.BenchParameterized.bench1,
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.OtherBench.bench3,
            JarTestHelper.BenchParameterized2.bench4
        )

        val m = BenchmarkIndexMapImpl(bs)

        val idxList = (0 until 4).toList().shuffled()

        val returnedBenchmarks = m.benchmarks(idxList).getOrHandle {
            Assertions.fail("could not get benchmarks: $it")
        }

        Assertions.assertEquals(idxList.size, returnedBenchmarks.size)

        returnedBenchmarks.forEachIndexed { i, b ->
            val expectedIdx = idxList[i]
            val expectedBenchmark = m[expectedIdx] ?: Assertions.fail("no benchmark for index $expectedIdx, should be $b")
            Assertions.assertEquals(expectedBenchmark, b)
        }
    }

    @Test
    fun fourBenchmarkGetBenchmarksTwo() {
        val bs = listOf(
            JarTestHelper.BenchParameterized.bench1,
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.OtherBench.bench3,
            JarTestHelper.BenchParameterized2.bench4
        )

        val m = BenchmarkIndexMapImpl(bs)

        val idxList = (0 until 4).toList().shuffled().take(2)

        val returnedBenchmarks = m.benchmarks(idxList).getOrHandle {
            Assertions.fail("could not get benchmarks: $it")
        }

        Assertions.assertEquals(idxList.size, returnedBenchmarks.size)

        returnedBenchmarks.forEachIndexed { i, b ->
            val expectedIdx = idxList[i]
            val expectedBenchmark = m[expectedIdx] ?: Assertions.fail("no benchmark for index $expectedIdx, should be $b")
            Assertions.assertEquals(expectedBenchmark, b)
        }
    }

    @Test
    fun fourBenchmarkGetBenchmarksFourShiftedIndices() {
        val bs = listOf(
            JarTestHelper.BenchParameterized.bench1,
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.OtherBench.bench3,
            JarTestHelper.BenchParameterized2.bench4
        )

        val startAt = -2
        val m = BenchmarkIndexMapImpl(bs, startAt)

        val idxList = (-2 until 2).toList().shuffled()

        val returnedBenchmarks = m.benchmarks(idxList).getOrHandle {
            Assertions.fail("could not get benchmarks: $it")
        }

        Assertions.assertEquals(idxList.size, returnedBenchmarks.size)

        returnedBenchmarks.forEachIndexed { i, b ->
            val expectedIdx = idxList[i]
            val expectedBenchmark = m[expectedIdx] ?: Assertions.fail("no benchmark for index $expectedIdx, should be $b")
            Assertions.assertEquals(expectedBenchmark, b)
        }
    }

    @Test
    fun oneBenchmarkGetIndex() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIndexMapImpl(bs)

        val idx = 0
        val returnedIndex = m[b1] ?: Assertions.fail("expected index for benchmark $b1")
        Assertions.assertEquals(idx, returnedIndex)
    }

    @Test
    fun oneBenchmarkGetIndicesEmpty() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIndexMapImpl(bs)

        val benchList = listOf<Benchmark>()

        val returnedIndices = m.indices(benchList).getOrHandle {
            Assertions.fail("could not get indices: $it")
        }

        Assertions.assertEquals(0, returnedIndices.size)
    }

    @Test
    fun oneBenchmarkGetIndicesOne() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIndexMapImpl(bs)

        val benchList = listOf(b1)

        val returnedIndices = m.indices(benchList).getOrHandle {
            Assertions.fail("could not get indices: $it")
        }

        Assertions.assertEquals(benchList.size, returnedIndices.size)

        returnedIndices.forEachIndexed { i, idx ->
            val expectedBenchmark = benchList[i]
            val expectedIndex = m[expectedBenchmark] ?: Assertions.fail("no index for benchmark $expectedBenchmark, should be $idx")
            Assertions.assertEquals(expectedIndex, idx)
        }
    }

    @Test
    fun fourBenchmarkGetIndicesFour() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val b2 = JarTestHelper.BenchNonParameterized.bench2
        val b3 = JarTestHelper.OtherBench.bench3
        val b4 = JarTestHelper.BenchParameterized2.bench4

        val bs = listOf(b1, b2, b3, b4)

        val m = BenchmarkIndexMapImpl(bs)

        val benchList = bs.shuffled()

        val returnedIndices = m.indices(benchList).getOrHandle {
            Assertions.fail("could not get indices: $it")
        }

        Assertions.assertEquals(benchList.size, returnedIndices.size)

        returnedIndices.forEachIndexed { i, idx ->
            val expectedBenchmark = benchList[i]
            val expectedIndex = m[expectedBenchmark] ?: Assertions.fail("no index for benchmark $expectedBenchmark, should be $idx")
            Assertions.assertEquals(expectedIndex, idx)
        }
    }

    @Test
    fun fourBenchmarkGetIndicesTwo() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val b2 = JarTestHelper.BenchNonParameterized.bench2
        val b3 = JarTestHelper.OtherBench.bench3
        val b4 = JarTestHelper.BenchParameterized2.bench4

        val bs = listOf(b1, b2, b3, b4)

        val m = BenchmarkIndexMapImpl(bs)

        val benchList = bs.shuffled().take(2)

        val returnedIndices = m.indices(benchList).getOrHandle {
            Assertions.fail("could not get indices: $it")
        }

        Assertions.assertEquals(benchList.size, returnedIndices.size)

        returnedIndices.forEachIndexed { i, idx ->
            val expectedBenchmark = benchList[i]
            val expectedIndex = m[expectedBenchmark] ?: Assertions.fail("no index for benchmark $expectedBenchmark, should be $idx")
            Assertions.assertEquals(expectedIndex, idx)
        }
    }

    @Test
    fun fourBenchmarkGetIndicesFourShiftedIndices() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val b2 = JarTestHelper.BenchNonParameterized.bench2
        val b3 = JarTestHelper.OtherBench.bench3
        val b4 = JarTestHelper.BenchParameterized2.bench4

        val bs = listOf(b1, b2, b3, b4)

        val startAt = -2
        val m = BenchmarkIndexMapImpl(bs, startAt)

        val benchList = bs.shuffled()

        val returnedIndices = m.indices(benchList).getOrHandle {
            Assertions.fail("could not get indices: $it")
        }

        Assertions.assertEquals(benchList.size, returnedIndices.size)

        returnedIndices.forEachIndexed { i, idx ->
            val expectedBenchmark = benchList[i]
            val expectedIndex = m[expectedBenchmark] ?: Assertions.fail("no index for benchmark $expectedBenchmark, should be $idx")
            Assertions.assertEquals(expectedIndex, idx)
        }
    }
}
