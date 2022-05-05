package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Constants
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.MethodComparator
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.*
import java.io.BufferedWriter
import java.io.OutputStream
import java.nio.charset.Charset

interface CGPrinter {
    fun print(cgr: CGResult)
}

class SimpleCGPrinter(
        private val out: OutputStream,
        private val charset: Charset = Constants.defaultCharset
) : CGPrinter {

    override fun print(cgr: CGResult) {
        out.bufferedWriter(charset).use { w ->
            cgr.calls.toSortedMap(MethodComparator).forEach { (m, methods) ->
                w.write(C.cgStart)
                w.newLine()
                w.write(m.toString())
                w.newLine()
                methods.reachabilities().toSortedSet(ReachabilityResultComparator).forEach {
                    this.print(w, it)
                    w.newLine()
                }
            }
            w.flush()
        }
    }

    private fun print(w: BufferedWriter, r: ReachabilityResult) =
            when (r) {
                is PossiblyReachable -> print(w, r.to, r.probability, r.level)
                is Reachable -> print(w, r.to, 1.0, r.level)
                is NotReachable -> null
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
        private val C = CGPrinterReaderConstants
    }
}
