package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.Method
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter

interface MethodWeightPrinter {
    fun print(ws: MethodWeights)
}

class CSVMethodWeightPrinter(
        out: OutputStream,
        private val printHeader: Boolean = true,
        charset: String = Constants.defaultCharset
) : MethodWeightPrinter {

    private val out: BufferedWriter = BufferedWriter(OutputStreamWriter(out, charset))

    override fun print(ws: MethodWeights) {
        printHeader()
        ws.forEach { (m, w) -> print(m, w) }
        out.flush()
        out.close()
    }

    private fun print(m: Method, w: Double) {
        out.write(m.clazz)
        out.write(del)
        out.write(m.name)
        out.write(del)
        out.write(m.params.joinToString(separator = ","))
        out.write(del)
        out.write(w.toString())
        out.newLine()
    }

    private fun printHeader() {
        if (printHeader) {
            out.write(CSVMethodWeightConstants.clazz)
            out.write(del)
            out.write(CSVMethodWeightConstants.method)
            out.write(del)
            out.write(CSVMethodWeightConstants.params)
            out.write(del)
            out.write(CSVMethodWeightConstants.value)
            out.newLine()
            out.flush()
        }
    }

    companion object {
        const val del = ";"
    }
}
