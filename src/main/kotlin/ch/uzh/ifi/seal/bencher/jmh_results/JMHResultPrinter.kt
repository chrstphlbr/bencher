package ch.uzh.ifi.seal.bencher.jmh_results

import ch.uzh.ifi.seal.bencher.Constants
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter

interface JMHResultPrinter {
    fun print(res: JMHResult)
}

class JSONResultPrinter(
        os: OutputStream,
        charset: String = Constants.defaultCharset,
        private val flushPoint: FlushPoint = FlushPoint.Benchmark
) : JMHResultPrinter {

    private val csvHeader = "project,commit,benchmark,trial,fork,iteration,mode,unit,value"
    private val csvLine = "%s,%s,%s,%d,%d,%d,%s,%s,%f"

    private val w: BufferedWriter = BufferedWriter(OutputStreamWriter(os, charset))

    override fun print(res: JMHResult) {
        printHeader()
        res.benchmarks.forEach { br ->
            br.values.forEach { forkResult ->
                forkResult.iterations.forEach { iterResult ->
                    w.write(csvLine.format(
                            res.project,
                            res.commit,
                            br.name,
                            res.trial,
                            forkResult.fork,
                            iterResult.iteration,
                            br.mode,
                            br.unit,
                            iterResult.value
                    ))
                    w.write("\n")
                    flush()
                }
                flush()
            }
            flush()
        }
        flush()
        w.close()
    }

    private fun printHeader() {
        w.write(csvHeader)
        w.write("\n")
        w.flush()
    }

    private fun flush() {
        when (flushPoint) {
            FlushPoint.Iteration -> w.flush()
            FlushPoint.Fork -> w.flush()
            FlushPoint.Benchmark -> w.flush()
            FlushPoint.End -> w.flush()
        }
    }
}

enum class FlushPoint {
    Iteration, Fork, Benchmark, End
}
