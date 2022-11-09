package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitMethod
import java.io.BufferedWriter
import java.io.OutputStream
import java.nio.charset.Charset

interface CoverageUnitWeightPrinter {
    fun print(ws: CoverageUnitWeights)
}

class CSVMethodWeightPrinter(
        private val out: OutputStream,
        private val printHeader: Boolean = true,
        private val charset: Charset = Constants.defaultCharset
) : CoverageUnitWeightPrinter {

    override fun print(ws: CoverageUnitWeights) {
        out.bufferedWriter(charset).use { writer ->
            printHeader(writer)
            ws.forEach { (cu, w) ->
                // only print method coverages
                if (cu is CoverageUnitMethod) {
                    print(writer, cu.method, w)
                }
            }
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
