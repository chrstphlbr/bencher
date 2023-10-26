package ch.uzh.ifi.seal.bencher.measurement

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.ID
import ch.uzh.ifi.seal.bencher.Version
import ch.uzh.ifi.seal.bencher.VersionPair
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CSVPerformanceChangesReaderTest {

    private fun header(del: Char = DEFAULT_DEL): String = listOf(
        "id", "name", "function_params", "perf_params", "v1", "v2", "min", "max", "type",
    ).joinToString(del.toString())

    private fun differentialTest(csv: String, hasHeader: Boolean, del: Char) {
        val r1 = CSVPerformanceChangesReader(hasHeader = hasHeader, del = del)
        val pc1 = r1.read(csv.byteInputStream()).getOrElse { Assertions.fail("could not read input") }

        val memId = "mem-id"

        val r2 = CSVPerformanceChangesReader(hasHeader = hasHeader, del = del)
        val pc21 = r2.read(csv.byteInputStream()).getOrElse { Assertions.fail("could not read input") }
        assertEquals(pc1, pc21)

        val pc22 = r2.readAndMemorize(csv.byteInputStream(), memId).getOrElse { Assertions.fail("could not read input") }

        assertEquals(pc21, pc22)

        val pc23 = r2.get(memId).getOrElse { Assertions.fail("could not get for id $memId") }
        assertEquals(pc21, pc23)
    }

    private fun assertEquals(pc1: PerformanceChanges, pc2: PerformanceChanges) {
        val bs1 = pc1.benchmarks()
        val bs2 = pc2.benchmarks()
        Assertions.assertEquals(bs1.size, bs2.size)
        assertEquals(bs1, bs2)

        val vs1 = pc1.versions()
        val vs2 = pc2.versions()
        Assertions.assertEquals(vs1.size, vs2.size)

        Assertions.assertEquals(vs1, vs2, "pc1.versions() != pc2.versions()")
    }

    private fun assertEquals(bs1: Iterable<Benchmark>, bs2: Iterable<Benchmark>) {
        // Benchmark equals does not work as expected as Benchmark contains Lists -> use IDs (String representations)
        val expectedIDs = bs1.map { ID.string(it) }.sorted()
        val gotIDs = bs2.map { ID.string(it) }.sorted()

        Assertions.assertEquals(expectedIDs, gotIDs, "bs1 != bs2")
    }

    @ParameterizedTest
    @ValueSource(chars = [',', ';'])
    fun delimiter(del: Char) {
        val h = header(del)
        val csv ="""
            |$h
            |
            |""".trimMargin()

        val input = csv.byteInputStream()

        val r = CSVPerformanceChangesReader(hasHeader = true, del = del)
        r.read(input).getOrElse { Assertions.fail("could not read input") }

        differentialTest(csv, true, del)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun header(addHeader: Boolean) {
        val h = if (addHeader) {
            header()
        } else {
            ""
        }

        val csv ="""
            |$h
            |
            |""".trimMargin()

        val input = csv.byteInputStream()

        val r = CSVPerformanceChangesReader(hasHeader = addHeader)
        r.read(input).getOrElse { Assertions.fail("could not read input") }

        differentialTest(csv, addHeader, DEFAULT_DEL)
    }

    @Test
    fun noPerformanceChanges() {
        val h = header()

        val csv ="""
            |$h
            |
            |""".trimMargin()

        val input = csv.byteInputStream()

        val r = CSVPerformanceChangesReader(hasHeader = true)
        val pc = r.read(input).getOrElse { Assertions.fail("could not read input") }

        Assertions.assertTrue(pc.benchmarks().isEmpty())
        Assertions.assertTrue(pc.versions().isEmpty())

        differentialTest(csv, true, DEFAULT_DEL)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun multipleBenchmarksMultipleChanges(addHeader: Boolean) {
        // start setup

        val benchmarkChanges = listOf(
            Pair(JarTestHelper.BenchParameterized.bench1, PerformanceChangesTestHelper.bench1Changes),
            Pair(JarTestHelper.BenchNonParameterized.bench2, PerformanceChangesTestHelper.bench2Changes),
            Pair(JarTestHelper.OtherBench.bench3, PerformanceChangesTestHelper.bench3Changes),
            Pair(JarTestHelper.BenchParameterized2.bench4, PerformanceChangesTestHelper.bench4Changes),
        )

        val expectedBenchmarks = benchmarkChanges.map { it.first }
        val expectedVersionPairs = benchmarkChanges
            .flatMap { (_, cs) ->
                cs.map { c ->
                    VersionPair(c.v1, c.v2)
                }
            }
            .toSet()

        val benchmarkChangesCSV = benchmarkChanges.flatMap { (_, changes) ->
            changes.map { c ->
                // "id", "name", "function_params", "perf_params", "v1", "v2", "min", "max", "type",
                val b = c.benchmark
                listOf(
                    b.jmhID(),
                    "${b.clazz}.${b.name}",
                    b.params.joinToString(","),
                    b.jmhParams.joinToString(",") { "${it.first}=${it.second}" },
                    Version.toString(c.v1),
                    Version.toString(c.v2),
                    c.min,
                    c.max,
                    PerformanceChangeType.toString(c.type),
                ).joinToString(DEFAULT_DEL.toString())
            }
        }.joinToString("\n")

        // end setup

        val sb = StringBuilder()

        if (addHeader) {
            sb.appendLine(header())
        }

        sb.appendLine(benchmarkChangesCSV)
        sb.appendLine()

        val csv = sb.toString()

        val input = csv.byteInputStream()

        val r = CSVPerformanceChangesReader(hasHeader = addHeader)
        val pc = r.read(input).getOrElse { Assertions.fail("could not read input") }

        val gotBenchmarks = pc.benchmarks()
        Assertions.assertEquals(expectedBenchmarks.size, gotBenchmarks.size)

        assertEquals(expectedBenchmarks, gotBenchmarks)

        val gotVersionPairs = pc.versions()
        Assertions.assertEquals(expectedVersionPairs.size, gotVersionPairs.size)
        Assertions.assertEquals(expectedVersionPairs, gotVersionPairs, "expectedVersionPairs != pc.versions()")

        differentialTest(csv, addHeader, DEFAULT_DEL)
    }

    companion object {
        private const val DEFAULT_DEL = ';'
    }
}
