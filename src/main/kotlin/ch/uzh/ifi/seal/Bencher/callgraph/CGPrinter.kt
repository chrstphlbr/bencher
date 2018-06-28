package ch.uzh.ifi.seal.Bencher.callgraph

import ch.uzh.ifi.seal.Bencher.Constants
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter

interface CGPrinter {
    fun print(cg: CGResult)
}

class SimplePrinter(private val out: OutputStream, val charset: String = Constants.defaultCharset) : CGPrinter {
    private val w: BufferedWriter

    init {
        w = BufferedWriter(OutputStreamWriter(out, charset))
    }

    override fun print(cg: CGResult) {
        cg.calls.forEach { c ->
            w.write(c.toString())
            w.write("\n")
        }
        w.flush()
        w.close()
    }
}

