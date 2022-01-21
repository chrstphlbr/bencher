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

interface MultiPrioPrinter<T: Method> {
    fun printMulti(l: List<List<PrioritizedMethod<T>>>)
}

abstract class BenchmarkPrioPrinter(
        protected val out: OutputStream,
        protected val charset: Charset = Constants.defaultCharset
) : PrioPrinter<Benchmark>, MultiPrioPrinter<Benchmark>

class CSVPrioPrinter(out: OutputStream) : BenchmarkPrioPrinter(out) {

    override fun print(l: List<PrioritizedMethod<Benchmark>>) {
        out.bufferedWriter(charset).use { w ->
            writeHeader(w, false)
            l.forEach { pb ->
                writeLine(w, null, pb)
            }
            w.flush()
        }
    }

    override fun printMulti(l: List<List<PrioritizedMethod<Benchmark>>>) {
        out.bufferedWriter(charset).use { w ->
            writeHeader(w, true)
            l.forEachIndexed { i, s ->
                s.forEach { pb ->
                    writeLine(w, i + 1, pb)
                }
            }
            w.flush()
        }
    }

    private fun writeHeader(w: BufferedWriter, multi: Boolean) {
        val columns = mutableListOf<String>()
        columns.add("benchmark")
        columns.add("params")
        columns.add("perf_params")
        if (multi) {
            columns.add("solution")
        }
        columns.add("rank")
        columns.add("total")
        columns.add("prio")

        w.write(columns.joinToString(colSep))
        w.newLine()
        w.flush()
    }

    private fun writeLine(w: BufferedWriter, solutionNr: Int?, pb: PrioritizedMethod<Benchmark>) {
        val m = pb.method
        val prio = pb.priority
        val name = "${m.clazz}.${m.name}"
        val paramString = params(m)
        val jmhParamString = jmhParams(m)
        val value: String = prioValue(pb.priority.value)

        val columns = mutableListOf<String>()
        columns.add(name)
        columns.add(paramString)
        columns.add(jmhParamString)
        if (solutionNr != null) {
            columns.add(solutionNr.toString())
        }
        columns.add(prio.rank.toString())
        columns.add(prio.total.toString())
        columns.add(value)

        w.write(columns.joinToString(colSep))
        w.newLine()
    }

    private fun params(b: Benchmark): String = b.params.joinToString(separator = valSep)

    private fun jmhParams(b: Benchmark): String =
        b.jmhParams.joinToString(separator = valSep) { "${it.first}=${it.second}" }

    private fun prioValue(v: PriorityValue): String = when (v) {
        is PrioritySingle -> v.value.toString()
        is PriorityMultiple -> v.values.joinToString(valSep)
    }

    companion object {
        private const val colSep = ";"
        private const val valSep = ","
    }
}
