package ch.uzh.ifi.seal.bencher.analysis.coverage

import ch.uzh.ifi.seal.bencher.Constants
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
        private val coverageUnitType: CoverageUnitType,
        private val charset: Charset = Constants.defaultCharset
) : CoveragePrinter {

    override fun print(cov: Coverages) {
        out.bufferedWriter(charset).use { w ->
            cov.coverages.toSortedMap(MethodComparator).forEach { (m, cov) ->
                w.write(C.covStart)
                w.newLine()
                w.write(m.toString())
                w.newLine()
                cov.all().toSortedSet(CoverageUnitResultComparator).forEach {
                    val printed = print(w, it)
                    if (printed) {
                        w.newLine()
                    }
                }
            }
            w.flush()
        }
    }

    private fun print(w: BufferedWriter, r: CoverageUnitResult): Boolean {
        if (
            (r.unit is CoverageUnitMethod) && (coverageUnitType == CoverageUnitType.LINE) ||
            (r.unit is CoverageUnitLine) && (coverageUnitType == CoverageUnitType.METHOD)
        ) {
            return false
        }

        return when (r) {
            is PossiblyCovered -> print(w, r.unit, r.probability, r.level)
            is Covered -> print(w, r.unit, 1.0, r.level)
            is NotCovered -> false
        }
    }


    private fun print(w: BufferedWriter, unit: CoverageUnit, probability: Double, level: Int): Boolean {
        when (unit) {
            is CoverageUnitMethod -> print(w, unit)
            is CoverageUnitLine -> print(w, unit)
        }
        w.write(C.coverageLineDelimiter)
        w.write(probability.toString())
        w.write(C.coverageLineDelimiter)
        w.write(level.toString())
        return true
    }

    private fun print(w: BufferedWriter, cu: CoverageUnitMethod): Boolean {
        val m = cu.method

        w.write(C.methodStart)
        w.write(C.paramBracesOpen.toString())

        // clazz
        printParam(w, C.paramClazz, m.clazz)

        // name
        printParam(w, C.paramMethod, m.name)

        // params
        w.write(C.paramParams)
        w.write(C.paramAssignOp.toString())
        w.write(C.paramListStart.toString())
        w.write(m.params.joinToString(separator = C.paramDelimiter))
        w.write(C.paramListEnd.toString())

        w.write(C.paramBracesClosed.toString())
        return true
    }

    private fun print(w: BufferedWriter, m: CoverageUnitLine): Boolean {
        w.write(C.lineStart)
        w.write(C.paramBracesOpen.toString())

        // file
        printParam(w, C.paramFile, m.line.file)

        // line number
        printParam(w, C.paramNumber, m.line.number)

        // missed instructions
        printParam(w, C.paramMissedInstructions, m.missedInstructions)

        // covered instructions
        printParam(w, C.paramCoveredInstructions, m.coveredInstructions)

        // missed branches
        printParam(w, C.paramMissedBranches, m.missedBranches)

        // covered branches
        printParam(w, C.paramCoveredBranches, m.coveredBranches, addDelimiter = false)

        w.write(C.paramBracesClosed.toString())
        return true
    }

    private fun <T> printParam(w: BufferedWriter, paramName: String, paramValue: T?, addDelimiter: Boolean = true) {
        w.write(paramName)
        w.write(C.paramAssignOp.toString())
        val v = paramValue ?: C.valueNull
        w.write(v.toString())

        if (addDelimiter) {
            w.write(C.paramDelimiter)
        }
    }

    companion object {
        private val C = CoveragePrinterReaderConstants
    }
}
