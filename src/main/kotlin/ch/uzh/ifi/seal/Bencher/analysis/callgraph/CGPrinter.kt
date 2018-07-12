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
        val charset: String = Constants.defaultCharset
) : CGPrinter {

    private val w: BufferedWriter

    init {
        w = BufferedWriter(OutputStreamWriter(out, charset))
    }

    override fun print(cgr: CGResult) {
        cgr.cg.calls.forEach { c ->
            w.write(c.toString())
            w.write("\n")
        }
        w.flush()
        w.close()
    }
}
