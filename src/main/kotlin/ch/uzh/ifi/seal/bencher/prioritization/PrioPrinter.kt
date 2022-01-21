package ch.uzh.ifi.seal.bencher.prioritization

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.Method
import java.io.BufferedWriter
import java.io.OutputStream
import java.nio.charset.Charset

interface PrioPrinter<T : Method> {
    fun print(l: List<PrioritizedMethod<T>>)
}

abstract class BenchmarkPrioPrinter(
        protected val out: OutputStream,
        protected val charset: Charset = Constants.defaultCharset
) : PrioPrinter<Benchmark>

class CSVPrioPrinter(out: OutputStream) : BenchmarkPrioPrinter(out) {

    override fun print(l: List<PrioritizedMethod<Benchmark>>) {
        out.bufferedWriter(charset).use { w ->
            writeHeader(w)
            l.forEach { pb ->
                val m = pb.method
                val prio = pb.priority
                val paramString = m.params.joinToString(separator = ",")
                val jmhParamString = m.jmhParams.joinToString(separator = ",") { "${it.first}=${it.second}" }
                val value: String = when (val v = prio.value) {
                    is PrioritySingle -> v.value.toString()
                    is PriorityMultiple -> v.values.joinToString(",")
                }

                w.write("${m.clazz}.${m.name};$paramString;$jmhParamString;${prio.rank};${prio.total};$value")
                w.newLine()
            }
            w.flush()
        }
    }

    private fun writeHeader(w: BufferedWriter) {
        w.write("benchmark;params;perf_params;rank;total;prio")
        w.newLine()
        w.flush()
    }
}
