package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.Method
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter

interface PrioPrinter<T : Method> {
    fun print(l: List<PrioritizedMethod<T>>)
}

abstract class BenchmarkPrioPrinter(
        protected val out: OutputStream,
        protected val charset: String = Constants.defaultCharset
) : PrioPrinter<Benchmark>

class CSVPrioPrinter(out: OutputStream) : BenchmarkPrioPrinter(out) {

    private val w: BufferedWriter = BufferedWriter(OutputStreamWriter(out, charset))

    override fun print(l: List<PrioritizedMethod<Benchmark>>) {
        writeHeader()
        l.forEach { pb ->
            val m = pb.method
            val prio = pb.priority
            val paramString = m.params.joinToString(separator = ",")
            val jmhParamString = m.jmhParams.joinToString(separator = ",") { "${it.first}=${it.second}" }
            w.write("${m.clazz}.${m.name};$paramString;$jmhParamString;${prio.rank};${prio.total};${prio.value}")
            w.newLine()
        }
        w.flush()
        w.close()
    }

    private fun writeHeader() {
        w.write("benchmark;params;perf_params;rank;total;prio")
        w.newLine()
        w.flush()
    }
}
