package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.MethodComparator
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter

interface CGPrinter {
    fun print(cgr: CGResult)
}

class SimpleCGPrinter(
        out: OutputStream,
        charset: String = Constants.defaultCharset,
        private val closeOut: Boolean = true
) : CGPrinter {

    private val w: BufferedWriter = BufferedWriter(OutputStreamWriter(out, charset))

    override fun print(cgr: CGResult) {
        cgr.calls.toSortedMap(MethodComparator).forEach { (m, methods) ->
            w.write(C.cgStart)
            w.newLine()
            w.write(m.toString())
            w.newLine()
            methods.toSortedSet(MethodCallComparator).forEach {
                this.print(it)
                w.newLine()
            }
        }
        w.flush()
        if (closeOut) {
            w.close()
        }
    }

    private fun print(mc: MethodCall) {
        this.print(mc.from)
        w.write(C.edgeLineDelimiter)
        w.write(mc.idPossibleTargets.toString())
        w.write(C.edgeLineDelimiter)
        w.write(mc.nrPossibleTargets.toString())
        w.write(C.edgeLineDelimiter)
        this.print(mc.to)
    }

    private fun print(m: Method) {
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
        private val C = CGPrinterReaderConstants
    }
}
