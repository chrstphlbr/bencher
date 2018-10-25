package ch.uzh.ifi.seal.bencher.jmh_results

import ch.uzh.ifi.seal.bencher.Constants
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter

interface JMHResultPrinter {
    fun printHeader()
    fun done()
    fun print(project: String, commit: String, trial: Int, benchmarkResult: BenchmarkResult)

    fun printAll(res: JMHResult) {
        printHeader()
        res.benchmarks.forEach {
            print(res.project, res.commit, res.trial, it)
        }
        done()
    }
}

class JSONResultPrinter(
        os: OutputStream,
        charset: String = Constants.defaultCharset,
        private val repeatHistogramValues: Boolean = false,
        private val flushPoint: FlushPoint = FlushPoint.Benchmark
) : JMHResultPrinter {
    private val csvHeader = "project;commit;benchmark;params;trial;fork;iteration;mode;unit;value_count;value"
    private val csvLine = "%s;%s;%s;%s;%d;%d;%d;%s;%s;%d;%e"

    private val w: BufferedWriter = BufferedWriter(OutputStreamWriter(os, charset))

    override fun print(project: String, commit: String, trial: Int, benchmarkResult: BenchmarkResult) {
        benchmarkResult.values.forEach { forkResult ->
            forkResult.iterations.forEach { iterResult ->
                iterResult.invocations.forEach { invocationResult ->
                    val jmhParams = benchmarkResult.jmhParams.fold("") { acc, e -> "$acc${e.first}=${e.second}," }.substringBeforeLast(",")
                    if (repeatHistogramValues) {
                        for (i in 1..invocationResult.count) {
                            w.write(csvLine.format(
                                    project,
                                    commit,
                                    benchmarkResult.name,
                                    jmhParams,
                                    trial,
                                    forkResult.fork,
                                    iterResult.iteration,
                                    benchmarkResult.mode,
                                    benchmarkResult.unit,
                                    1,
                                    invocationResult.value
                            ))
                        }
                    } else {
                        w.write(csvLine.format(
                                project,
                                commit,
                                benchmarkResult.name,
                                jmhParams,
                                trial,
                                forkResult.fork,
                                iterResult.iteration,
                                benchmarkResult.mode,
                                benchmarkResult.unit,
                                invocationResult.count,
                                invocationResult.value
                        ))
                    }

                    w.write("\n")
                    flush()
                }
                flush()
            }
            flush()
        }
        flush()
    }

    override fun printHeader() {
        w.write(csvHeader)
        w.write("\n")
        w.flush()
    }

    override fun done() {
        flush()
        w.close()
    }


    private fun flush() {
        when (flushPoint) {
            FlushPoint.Invocation -> w.flush()
            FlushPoint.Iteration -> w.flush()
            FlushPoint.Fork -> w.flush()
            FlushPoint.Benchmark -> w.flush()
            FlushPoint.End -> w.flush()
        }
    }
}

enum class FlushPoint {
    Invocation, Iteration, Fork, Benchmark, End
}
