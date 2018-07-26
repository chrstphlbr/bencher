package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Constants
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter

interface CGPrinter {
    fun print(cgr: CGResult)
}

class SimplePrinter(
        out: OutputStream,
        val indent: String = SimplePrinter.defaultIndent,
        val charset: String = Constants.defaultCharset
) : CGPrinter {

    private val w: BufferedWriter

    init {
        w = BufferedWriter(OutputStreamWriter(out, charset))
    }

    override fun print(cgr: CGResult) {
        cgr.benchCalls.forEach { (bench, methods) ->
            w.write(benchStart)
            w.newLine()
            w.write(bench.toString())
            w.newLine()
            methods.forEach { m ->
                1.rangeTo(m.level).forEach {
                    w.write(indent)
                }
                w.write(m.method.toString())
                w.newLine()
            }
        }
        w.flush()
        w.close()
    }

    companion object {
        val defaultIndent = "    "
        val benchStart = "Benchmark:"
    }
}
