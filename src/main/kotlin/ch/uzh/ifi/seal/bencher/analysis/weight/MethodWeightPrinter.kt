package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.Method
import java.io.BufferedWriter
import java.io.OutputStream
import java.nio.charset.Charset

interface MethodWeightPrinter {
    fun print(ws: MethodWeights)
}

class CSVMethodWeightPrinter(
        private val out: OutputStream,
        private val printHeader: Boolean = true,
        private val charset: Charset = Constants.defaultCharset
) : MethodWeightPrinter {

    override fun print(ws: MethodWeights) {
        out.bufferedWriter(charset).use { writer ->
            printHeader(writer)
            ws.forEach { (m, w) -> print(writer, m, w) }
            writer.flush()
        }
    }

    private fun print(writer: BufferedWriter, m: Method, w: Double) {
        writer.write(m.clazz)
        writer.write(del)
        writer.write(m.name)
        writer.write(del)
        writer.write(m.params.joinToString(separator = ","))
        writer.write(del)
        writer.write(w.toString())
        writer.newLine()
    }

    private fun printHeader(w: BufferedWriter) {
        if (printHeader) {
            w.write(CSVMethodWeightConstants.clazz)
            w.write(del)
            w.write(CSVMethodWeightConstants.method)
            w.write(del)
            w.write(CSVMethodWeightConstants.params)
            w.write(del)
            w.write(CSVMethodWeightConstants.value)
            w.newLine()
            w.flush()
        }
    }

    companion object {
        const val del = ";"
    }
}
