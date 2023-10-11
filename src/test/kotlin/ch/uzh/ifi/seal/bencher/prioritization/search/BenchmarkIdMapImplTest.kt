package ch.uzh.ifi.seal.bencher.prioritization.search

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BenchmarkIdMapImplTest {

    @Test
    fun empty() {
        val bs = listOf<Benchmark>()
        val m = BenchmarkIdMapImpl(bs)
        Assertions.assertEquals(0, m.size)
    }

    @Test
    fun oneBenchmarkSize() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIdMapImpl(bs)

        Assertions.assertEquals(bs.size, m.size)
    }

    @Test
    fun oneBenchmarkGetBenchmark() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIdMapImpl(bs)

        val id = 0
        val returnedBenchmark = m[id] ?: Assertions.fail("expected benchmark for id $id")
        Assertions.assertEquals(b1, returnedBenchmark)
    }

    @Test
    fun oneBenchmarkGetBenchmarksEmpty() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIdMapImpl(bs)

        val idsList = listOf<Int>()

        val returnedBenchmarks = m.benchmarks(idsList).getOrElse {
            Assertions.fail("could not get benchmarks: $it")
        }

        Assertions.assertEquals(0, returnedBenchmarks.size)
    }

    @Test
    fun oneBenchmarkGetBenchmarksOne() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIdMapImpl(bs)

        val id = 0
        val idsList = listOf(id)

        val returnedBenchmarks = m.benchmarks(idsList).getOrElse {
            Assertions.fail("could not get benchmarks: $it")
        }

        Assertions.assertEquals(idsList.size, returnedBenchmarks.size)

        returnedBenchmarks.forEachIndexed { i, b ->
            val expectedId = idsList[i]
            val expectedBenchmark = m[expectedId] ?: Assertions.fail("no benchmark for id $expectedId, should be $b")
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

        val m = BenchmarkIdMapImpl(bs)

        val idsList = (0 until 4).toList().shuffled()

        val returnedBenchmarks = m.benchmarks(idsList).getOrElse {
            Assertions.fail("could not get benchmarks: $it")
        }

        Assertions.assertEquals(idsList.size, returnedBenchmarks.size)

        returnedBenchmarks.forEachIndexed { i, b ->
            val expectedId = idsList[i]
            val expectedBenchmark = m[expectedId] ?: Assertions.fail("no benchmark for id $expectedId, should be $b")
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

        val m = BenchmarkIdMapImpl(bs)

        val idsList = (0 until 4).toList().shuffled().take(2)

        val returnedBenchmarks = m.benchmarks(idsList).getOrElse {
            Assertions.fail("could not get benchmarks: $it")
        }

        Assertions.assertEquals(idsList.size, returnedBenchmarks.size)

        returnedBenchmarks.forEachIndexed { i, b ->
            val expectedId = idsList[i]
            val expectedBenchmark = m[expectedId] ?: Assertions.fail("no benchmark for id $expectedId, should be $b")
            Assertions.assertEquals(expectedBenchmark, b)
        }
    }

    @Test
    fun fourBenchmarkGetBenchmarksFourShiftedIds() {
        val bs = listOf(
            JarTestHelper.BenchParameterized.bench1,
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.OtherBench.bench3,
            JarTestHelper.BenchParameterized2.bench4
        )

        val startAt = -2
        val m = BenchmarkIdMapImpl(bs, startAt)

        val idsList = (-2 until 2).toList().shuffled()

        val returnedBenchmarks = m.benchmarks(idsList).getOrElse {
            Assertions.fail("could not get benchmarks: $it")
        }

        Assertions.assertEquals(idsList.size, returnedBenchmarks.size)

        returnedBenchmarks.forEachIndexed { i, b ->
            val expectedId = idsList[i]
            val expectedBenchmark = m[expectedId] ?: Assertions.fail("no benchmark for id $expectedId, should be $b")
            Assertions.assertEquals(expectedBenchmark, b)
        }
    }

    @Test
    fun oneBenchmarkGetId() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIdMapImpl(bs)

        val id = 0
        val returnedId = m[b1] ?: Assertions.fail("expected id for benchmark $b1")
        Assertions.assertEquals(id, returnedId)
    }

    @Test
    fun oneBenchmarkGetIdsEmpty() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIdMapImpl(bs)

        val benchList = listOf<Benchmark>()

        val returnedIds = m.ids(benchList).getOrElse {
            Assertions.fail("could not get ids: $it")
        }

        Assertions.assertEquals(0, returnedIds.size)
    }

    @Test
    fun oneBenchmarkGetIdsOne() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val bs = listOf(b1)

        val m = BenchmarkIdMapImpl(bs)

        val benchList = listOf(b1)

        val returnedIds = m.ids(benchList).getOrElse {
            Assertions.fail("could not get ids: $it")
        }

        Assertions.assertEquals(benchList.size, returnedIds.size)

        returnedIds.forEachIndexed { i, id ->
            val expectedBenchmark = benchList[i]
            val expectedId =
                m[expectedBenchmark] ?: Assertions.fail("no id for benchmark $expectedBenchmark, should be $id")
            Assertions.assertEquals(expectedId, id)
        }
    }

    @Test
    fun fourBenchmarkGetIdsFour() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val b2 = JarTestHelper.BenchNonParameterized.bench2
        val b3 = JarTestHelper.OtherBench.bench3
        val b4 = JarTestHelper.BenchParameterized2.bench4

        val bs = listOf(b1, b2, b3, b4)

        val m = BenchmarkIdMapImpl(bs)

        val benchList = bs.shuffled()

        val returnedIds = m.ids(benchList).getOrElse {
            Assertions.fail("could not get ids: $it")
        }

        Assertions.assertEquals(benchList.size, returnedIds.size)

        returnedIds.forEachIndexed { i, id ->
            val expectedBenchmark = benchList[i]
            val expectedId =
                m[expectedBenchmark] ?: Assertions.fail("no id for benchmark $expectedBenchmark, should be $id")
            Assertions.assertEquals(expectedId, id)
        }
    }

    @Test
    fun fourBenchmarkGetIdsTwo() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val b2 = JarTestHelper.BenchNonParameterized.bench2
        val b3 = JarTestHelper.OtherBench.bench3
        val b4 = JarTestHelper.BenchParameterized2.bench4

        val bs = listOf(b1, b2, b3, b4)

        val m = BenchmarkIdMapImpl(bs)

        val benchList = bs.shuffled().take(2)

        val returnedIds = m.ids(benchList).getOrElse {
            Assertions.fail("could not get ids: $it")
        }

        Assertions.assertEquals(benchList.size, returnedIds.size)

        returnedIds.forEachIndexed { i, id ->
            val expectedBenchmark = benchList[i]
            val expectedId =
                m[expectedBenchmark] ?: Assertions.fail("no id for benchmark $expectedBenchmark, should be $id")
            Assertions.assertEquals(expectedId, id)
        }
    }

    @Test
    fun fourBenchmarkGetIdsFourShiftedIds() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val b2 = JarTestHelper.BenchNonParameterized.bench2
        val b3 = JarTestHelper.OtherBench.bench3
        val b4 = JarTestHelper.BenchParameterized2.bench4

        val bs = listOf(b1, b2, b3, b4)

        val startAt = -2
        val m = BenchmarkIdMapImpl(bs, startAt)

        val benchList = bs.shuffled()

        val returnedIds = m.ids(benchList).getOrElse {
            Assertions.fail("could not get ids: $it")
        }

        Assertions.assertEquals(benchList.size, returnedIds.size)

        returnedIds.forEachIndexed { i, id ->
            val expectedBenchmark = benchList[i]
            val expectedId =
                m[expectedBenchmark] ?: Assertions.fail("no id for benchmark $expectedBenchmark, should be $id")
            Assertions.assertEquals(expectedId, id)
        }
    }
}
