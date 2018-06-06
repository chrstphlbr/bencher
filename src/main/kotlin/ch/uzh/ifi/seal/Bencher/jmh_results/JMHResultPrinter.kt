package ch.uzh.ifi.seal.Bencher.jmh_results

import java.io.OutputStream
import java.io.OutputStreamWriter

interface JMHResultPrinter {
    fun print(res: JMHResult)
}

class JSONResultPrinter(val os: OutputStream, val flushPoint: FlushPoint = FlushPoint.Benchmark) : JMHResultPrinter {
    val csvHeader = "project,commit,benchmark,trial,fork,iteration,mode,unit,value"
    val csvLine = "%s,%s,%s,%d,%d,%d,%s,%s,%f"

    val ow: OutputStreamWriter
    init {
        ow = OutputStreamWriter(os)
    }

    override fun print(res: JMHResult) {
        printHeader()
        res.benchmarks.forEach { br ->
            br.values.forEach { forkResult ->
                forkResult.iterations.forEach { iterResult ->
                    ow.write(csvLine.format(
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
                    ow.write("\n")
                    flush()
                }
                flush()
            }
            flush()
        }
        flush()
        ow.close()
    }

    private fun printHeader() {
        ow.write(csvHeader)
        ow.write("\n")
        ow.flush()
    }

    private fun flush() {
        when (flushPoint) {
            FlushPoint.Iteration -> ow.flush()
            FlushPoint.Fork -> ow.flush()
            FlushPoint.Benchmark -> ow.flush()
            FlushPoint.End -> ow.flush()
        }
    }
}

enum class FlushPoint {
    Iteration, Fork, Benchmark, End
}