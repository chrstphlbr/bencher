package ch.uzh.ifi.seal.bencher.analysis.coverage

import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.MethodComparator
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.*
import java.io.BufferedWriter
import java.io.OutputStream
import java.nio.charset.Charset

interface CoveragePrinter {
    fun print(cov: Coverages)
}

class SimpleCoveragePrinter(
        private val out: OutputStream,
        private val charset: Charset = Constants.defaultCharset
) : CoveragePrinter {

    override fun print(cov: Coverages) {
        out.bufferedWriter(charset).use { w ->
            cov.coverages.toSortedMap(MethodComparator).forEach { (m, methods) ->
                w.write(C.covStart)
                w.newLine()
                w.write(m.toString())
                w.newLine()
                methods.all().toSortedSet(CoverageUnitResultComparator).forEach {
                    this.print(w, it)
                    w.newLine()
                }
            }
            w.flush()
        }
    }

    private fun print(w: BufferedWriter, r: CoverageUnitResult) =
            when (r) {
                is PossiblyCovered -> print(w, r.unit, r.probability, r.level)
                is Covered -> print(w, r.unit, 1.0, r.level)
                is NotCovered -> null
            }

    private fun print(w: BufferedWriter, to: Method, probability: Double, level: Int) {
        this.print(w, to)
        w.write(C.edgeLineDelimiter)
        w.write(probability.toString())
        w.write(C.edgeLineDelimiter)
        w.write(level.toString())
    }

    private fun print(w: BufferedWriter, m: Method) {
        w.write(C.methodStart)
        w.write("(")
        // clazz
        w.write(C.paramClazz)
        w.write(C.paramAssignOp.toString())
        w.write(m.clazz)

        w.write(C.paramDelimiter)

        // name
        w.write(C.paramMethod)
        w.write(C.paramAssignOp.toString())
        w.write(m.name)

        w.write(C.paramDelimiter)

        // params
        w.write(C.paramParams)
        w.write(C.paramAssignOp.toString())
        w.write(C.paramListStart.toString())
        w.write(m.params.joinToString(separator = C.paramDelimiter))
        w.write(C.paramListEnd.toString())

        w.write(")")
    }

    companion object {
        private val C = CoveragePrinterReaderConstants
    }
}
