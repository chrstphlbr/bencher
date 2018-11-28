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
        private val indent: String = SimplePrinter.defaultIndent,
        charset: String = Constants.defaultCharset
) : CGPrinter {

    private val w: BufferedWriter = BufferedWriter(OutputStreamWriter(out, charset))

    override fun print(cgr: CGResult) {
        cgr.calls.forEach { (m, methods) ->
            w.write(benchStart)
            w.newLine()
            w.write(m.toString())
            w.newLine()
            methods.forEach { cm ->
                1.rangeTo(cm.level).forEach {
                    w.write(indent)
                }
                w.write(cm.method.toString())
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
