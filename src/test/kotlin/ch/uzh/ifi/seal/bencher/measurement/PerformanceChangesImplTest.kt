package ch.uzh.ifi.seal.bencher.measurement

import arrow.core.None
import arrow.core.Some
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.VersionPair
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.*

class PerformanceChangesImplTest {

    private val pc = PerformanceChangesTestHelper.changes

    private val expectedBenchmarks = listOf(
        JarTestHelper.BenchParameterized.bench1,
        JarTestHelper.BenchNonParameterized.bench2,
        JarTestHelper.OtherBench.bench3,
        JarTestHelper.BenchParameterized2.bench4,
    )

    private val expectedBenchmarkChanges = listOf(
        Pair(expectedBenchmarks[0], PerformanceChangesTestHelper.bench1Changes),
        Pair(expectedBenchmarks[1], PerformanceChangesTestHelper.bench2Changes),
        Pair(expectedBenchmarks[2], PerformanceChangesTestHelper.bench3Changes),
        Pair(expectedBenchmarks[3], PerformanceChangesTestHelper.bench4Changes),
    )

    private val expectedBenchmarkSums = listOf(
        Pair(expectedBenchmarks[0], PerformanceChangesTestHelper.bench1MinChangeSum),
        Pair(expectedBenchmarks[1], PerformanceChangesTestHelper.bench2MinChangeSum),
        Pair(expectedBenchmarks[2], PerformanceChangesTestHelper.bench3MinChangeSum),
        Pair(expectedBenchmarks[3], PerformanceChangesTestHelper.bench4MinChangeSum),
    )

    @Test
    fun correctExpectations() {
        Assertions.assertEquals(expectedBenchmarks.size, expectedBenchmarkChanges.size)
        expectedBenchmarkChanges.forEach { (b, cs) ->
            cs.forEach { c ->
                Assertions.assertEquals(b, c.benchmark)
            }
        }
        Assertions.assertEquals(expectedBenchmarks.size, expectedBenchmarkSums.size)
    }

    @Test
    fun empty() {
        val pc = PerformanceChangesImpl(listOf())
        Assertions.assertTrue(pc.benchmarks().isEmpty())
        Assertions.assertTrue(pc.versions().isEmpty())
    }

    @Test
    fun benchmarks() {
        val benchmarks = pc.benchmarks()
        Assertions.assertEquals(expectedBenchmarks.size, benchmarks.size)
        Assertions.assertTrue(benchmarks.containsAll(expectedBenchmarks))
    }

    @Test
    fun versions() {
        val versionPairs = pc.versions()
        Assertions.assertEquals(PerformanceChangesTestHelper.versionPairs.size, versionPairs.size)
        Assertions.assertTrue(versionPairs.containsAll(PerformanceChangesTestHelper.versionPairs.toSet()))
    }

    private fun assertChanges(expectedChanges: List<PerformanceChange>, changes: List<PerformanceChange>) {
        Assertions.assertEquals(expectedChanges.size, changes.size, "expectedChanges.size != changes.size")
        Assertions.assertTrue(changes.containsAll(expectedChanges), "changes does not contain all expectedChanges")
        Assertions.assertTrue(expectedChanges.containsAll(changes), "expectedChanges does not contain all changes")
    }

    private fun checkChangesBenchmark(b: Benchmark, expectedChanges: List<PerformanceChange>) {
        val changes = pc.changes(b).getOrElse { Assertions.fail("no changes for benchmark $b") }
        assertChanges(expectedChanges, changes)
    }

    @Test
    fun changesBenchmark() {
        expectedBenchmarkChanges.forEach { (b, changes) ->
            checkChangesBenchmark(b, changes)
        }
    }

    private fun checkChangesVersion(versionPair: VersionPair) {
        val changes = pc.changes(versionPair.v1, versionPair.v2).getOrElse {
            Assertions.fail("no changes for version pair $versionPair")
        }

        val expectedChanges = PerformanceChangesTestHelper.allChanges.filter { pc ->
            pc.v1 == versionPair.v1 && pc.v2 == versionPair.v2
        }

        assertChanges(expectedChanges, changes)
    }

    @Test
    fun changesVersion() {
        PerformanceChangesTestHelper.versionPairs.forEach { versionPair ->
            checkChangesVersion(versionPair)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun changesUntilVersion(including: Boolean) {
        PerformanceChangesTestHelper.versions.forEach { v ->
            val changes = pc.changesUntilVersion(v, including).getOrElse {
                Assertions.fail("could not get changes until version $v (including=$including)")
            }

            val expectedChanges = PerformanceChangesTestHelper.allChanges.filter { pc ->
                if (including) {
                    pc.v2 <= v
                } else {
                    pc.v2 < v
                }
            }

            assertChanges(expectedChanges, changes)
        }
    }

    @Test
    fun benchmarkChangeStatisticEmptyChanges() {
        val pc = PerformanceChangesImpl(listOf())
        val b = JarTestHelper.BenchParameterized.bench1
        val stat = pc.benchmarkChangeStatistic(b, Mean)
        Assertions.assertEquals(None, stat)
    }

    @Test
    fun benchmarkChangeStatisticEmptyChangesDefault() {
        val r = Random(System.nanoTime())

        val pc = PerformanceChangesImpl(listOf())
        val b = JarTestHelper.BenchParameterized.bench1

        val default = r.nextDouble()

        val stat = pc.benchmarkChangeStatistic(b, Mean, Some(default)).getOrElse {
            Assertions.fail("expected default value instead of None")
        }

        Assertions.assertEquals(default, stat)
    }

    private fun checkBenchmarkChangeStatistic(b: Benchmark, statistic: Statistic<Int, Double>, expectedStat: Double) {
        val stat = pc.benchmarkChangeStatistic(b, statistic).getOrElse {
            Assertions.fail("expected statistic value")
        }
        Assertions.assertEquals(expectedStat, stat)
    }

    @Test
    fun benchmarkChangeStatistic() {
        val statistic = Sum
        expectedBenchmarkSums.forEach { (b, sum) -> checkBenchmarkChangeStatistic(b, statistic, sum) }
    }
}
