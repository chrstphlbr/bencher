package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutorMock
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Paths

class CSVPrioritizerTest {

    private fun csvPrios(del: Char): String =
            """
            org.sample.core.CoreA${del}m${del}1
            org.sample.core.CoreB${del}m${del}2
            org.sample.core.CoreC${del}m${del}3
            org.sample.core.CoreD${del}m${del}4
            """.trimIndent()

    private fun assertPriority(b: PrioritizedMethod<out Method>, rank: Int, total: Int, value: Double) {
        val r = b.priority.rank
        Assertions.assertTrue(r == rank, "Benchmark does not have priority rank $rank (was $r")
        val t = b.priority.total
        Assertions.assertTrue(t == total, "Benchmark does not have priority total $total (was $t")
        val v = b.priority.value
        Assertions.assertTrue(v == value, "Benchmark does not have priority value $value (was $v)")
    }

    private fun assertBenchmark(b: PrioritizedMethod<Benchmark>, expectedBench: Benchmark, rank: Int, total: Int, value: Double) {
        Assertions.assertTrue(b.method == expectedBench, "Benchmark not as expected: was ${b.method}, expected $expectedBench")
        assertPriority(b, rank, total, value)
    }

    @Test
    fun noPrios() {
        val p = CSVPrioritizer(
                file = "".byteInputStream(),
                del = ';',
                hasHeader = false,
                hasParams = false,
                cgExecutor = cgExecMockFull,
                jarFile = Paths.get("")
        )

        val eBenchs = p.prioritize(benchs)

        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not retrieve prioritized benchs: ${eBenchs.left().get() }}")
        }

        val bs = eBenchs.right().get()
        Assertions.assertTrue(bs.size == benchs.size)

        bs.forEach { b ->
            assertPriority(b, 1, 4, 0.0)
        }
    }

    private fun withPriosTest(prios: String, del: Char, hasHeader: Boolean) {
        val p = CSVPrioritizer(
                file = prios.byteInputStream(),
                del = del,
                hasHeader = hasHeader,
                hasParams = false,
                cgExecutor = cgExecMockFull,
                jarFile = Paths.get("")
        )

        val eBenchs = p.prioritize(benchs)

        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not retrieve prioritized benchs: ${eBenchs.left().get() }}")
        }

        val bs = eBenchs.right().get()
        Assertions.assertTrue(bs.size == benchs.size)

        val b1 = bs[0]
        assertBenchmark(b1, JarTestHelper.BenchParameterized.bench1, 1, 4, 6.0)

        // benchmarks 2 and 3 are equal with respect to their ranking and therefore are in arbitrary order
        val b2 = bs[1]
        assertPriority(b2, 2, 4, 5.0)
        val b3 = bs[2]
        assertPriority(b3, 2, 4, 5.0)

        val b2b3 = (JarTestHelper.OtherBench.bench3 == b2.method  && JarTestHelper.BenchParameterized2.bench4 == b3.method) ||
                (JarTestHelper.OtherBench.bench3 == b3.method && JarTestHelper.BenchParameterized2.bench4 == b2.method)
        Assertions.assertTrue(b2b3, "Benchmark 3 or 4 not in output at rank 2")


        val b4 = bs[3]
        assertBenchmark(b4, JarTestHelper.BenchNonParameterized.bench2, 4, 4, 3.0)
    }

    @ValueSource(chars = [',', ';'])
    @ParameterizedTest
    fun withPrios(del: Char) {
        withPriosTest(
                prios = csvPrios(del),
                del = del,
                hasHeader = false
        )
    }

    @ValueSource(chars = [',', ';'])
    @ParameterizedTest
    fun withPriosHeader(del: Char) {
        val prios = "${CSVPrioritizer.csvClass}${del}${CSVPrioritizer.csvMethod}${del}${CSVPrioritizer.csvValue}\n${csvPrios(del)}"
        println(prios)
        withPriosTest(
                prios = prios,
                del = del,
                hasHeader = true
        )
    }

    @Test
    fun benchsNotInCG() {
        val del = ','
        val p = CSVPrioritizer(
                file = csvPrios(del).byteInputStream(),
                del = del,
                hasHeader = false,
                hasParams = false,
                cgExecutor = cgExecMockTwo,
                jarFile = Paths.get("")
        )

        val eBenchs = p.prioritize(benchs)

        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not retrieve prioritized benchs: ${eBenchs.left().get() }}")
        }

        val bs = eBenchs.right().get()
        Assertions.assertTrue(bs.size == 2)

        val b1 = bs[0]
        assertBenchmark(b1, JarTestHelper.BenchParameterized.bench1, 1, 2, 6.0)

        val b2 = bs[1]
        assertBenchmark(b2, JarTestHelper.BenchNonParameterized.bench2, 2, 2, 3.0)
    }

    @Test
    fun noCGResults() {
        val del = ','
        val p = CSVPrioritizer(
                file = csvPrios(del).byteInputStream(),
                del = del,
                hasHeader = false,
                hasParams = false,
                cgExecutor = CGExecutorMock.new(),
                jarFile = Paths.get("")
        )

        val eBenchs = p.prioritize(benchs)

        if (eBenchs.isLeft()) {
            Assertions.fail<String>("Could not retrieve prioritized benchs: ${eBenchs.left().get() }}")
        }

        val benchs = eBenchs.right().get()
        Assertions.assertTrue(benchs.isEmpty(), "Exepected 0 benchmarks in prioritized list, because no CGResult available")
    }

    companion object {
        val benchs = listOf(
                JarTestHelper.BenchParameterized.bench1,
                JarTestHelper.BenchNonParameterized.bench2,
                JarTestHelper.OtherBench.bench3,
                JarTestHelper.BenchParameterized2.bench4
        )

        val cgExecMockFull = CGExecutorMock.new(CGTestHelper.b1Cg, CGTestHelper.b2Cg, CGTestHelper.b3Cg, CGTestHelper.b4Cg)
        val cgExecMockTwo = CGExecutorMock.new(CGTestHelper.b1Cg, CGTestHelper.b2Cg)
    }
}