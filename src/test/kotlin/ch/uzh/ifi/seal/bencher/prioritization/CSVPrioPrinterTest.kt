package ch.uzh.ifi.seal.bencher.prioritization

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class CSVPrioPrinterTest {
    private val header = "benchmark;params;perf_params;rank;total;prio"

    @Test
    fun empty() {
        val bos = ByteArrayOutputStream()
        val p = CSVPrioPrinter(out = bos)
        p.print(listOf())

        val out = String(bos.toByteArray())

        val lines = out.split("\n")
        Assertions.assertEquals(2, lines.size)
        // header line
        Assertions.assertEquals(header, lines[0])
        // last empty line
        Assertions.assertEquals("", lines[1])
    }

    private fun csvLine(pb: PrioritizedMethod<Benchmark>): String {
        val m = pb.method
        val p = pb.priority
        val fps = m.params.joinToString(separator = ",")
        val pps = m.jmhParams.joinToString(separator = ",") { "${it.first}=${it.second}" }
        val v: String = when (val v = p.value) {
            is PrioritySingle -> v.value.toString()
            is PriorityMultiple -> v.values.joinToString(",")
        }
        return "${m.clazz}.${m.name};$fps;$pps;${p.rank};${p.total};$v"
    }

    @Test
    fun singleBenchmarkSinglePriority() {
        val pbs = listOf(
                PrioritizedMethod(
                        method = JarTestHelper.BenchParameterized.bench1,
                        priority = Priority(rank = 1, total = 4, value = PrioritySingle(10.0))
                ),
                PrioritizedMethod(
                        method = JarTestHelper.BenchNonParameterized.bench2,
                        priority = Priority(rank = 2, total = 4, value = PrioritySingle(5.0))
                ),
                PrioritizedMethod(
                        method = JarTestHelper.OtherBench.bench3,
                        priority = Priority(rank = 3, total = 4, value = PrioritySingle(3.0))
                ),
                PrioritizedMethod(
                        method = JarTestHelper.BenchParameterized2.bench4,
                        priority = Priority(rank = 4, total = 4, value = PrioritySingle(2.5))
                )
        )

        val bos = ByteArrayOutputStream()
        val p = CSVPrioPrinter(out = bos)
        p.print(pbs)

        val out = String(bos.toByteArray())

        val lines = out.split("\n")
        Assertions.assertEquals(6, lines.size)
        // header line
        Assertions.assertEquals(header, lines[0])

        (1 until 5).forEach { i ->
            val l = lines[i]
            Assertions.assertEquals(csvLine(pbs[i - 1]), l)
        }

        // last empty line
        Assertions.assertEquals("", lines[5])
    }

    @Test
    fun singleBenchmarkMultiplePriorities() {
        val pbs = listOf(
            PrioritizedMethod(
                method = JarTestHelper.BenchParameterized.bench1,
                priority = Priority(rank = 1, total = 4, value = PriorityMultiple(listOf(10.0, 1.0, 5.0)))
            ),
            PrioritizedMethod(
                method = JarTestHelper.BenchNonParameterized.bench2,
                priority = Priority(rank = 2, total = 4, value = PriorityMultiple(listOf(5.0, 2.0)))
            ),
            PrioritizedMethod(
                method = JarTestHelper.OtherBench.bench3,
                priority = Priority(rank = 3, total = 4, value = PriorityMultiple(listOf(3.0)))
            ),
            PrioritizedMethod(
                method = JarTestHelper.BenchParameterized2.bench4,
                priority = Priority(rank = 4, total = 4, value = PriorityMultiple(listOf(2.5, 1.0, 10.0, 2.0, 4.0)))
            )
        )

        val bos = ByteArrayOutputStream()
        val p = CSVPrioPrinter(out = bos)
        p.print(pbs)

        val out = String(bos.toByteArray())

        val lines = out.split("\n")
        Assertions.assertEquals(6, lines.size)
        // header line
        Assertions.assertEquals(header, lines[0])

        (1 until 5).forEach { i ->
            val l = lines[i]
            Assertions.assertEquals(csvLine(pbs[i - 1]), l)
        }

        // last empty line
        Assertions.assertEquals("", lines[5])
    }
}
