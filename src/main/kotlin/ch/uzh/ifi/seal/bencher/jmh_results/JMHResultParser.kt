package ch.uzh.ifi.seal.bencher.jmh_results

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.funktionale.either.Either
import java.io.File
import java.io.FileInputStream

class JMHResultParser(
        private val inFile: File,
        private val project: String,
        private val commit: String,
        private val trial: Int) {

    private val jmhVersion = "jmhVersion"
    private val benchmark = "benchmark"
    private val mode = "mode"
    private val threads = "threads"
    private val forks = "forks"
    private val warmupIterations = "warmupIterations"
    private val warmupTime = "warmupTime"
    private val measurementIterations = "measurementIterations"
    private val measurementTime = "measurementTime"
    private val primaryMetric = "primaryMetric"
    private val unit = "scoreUnit"
    private val values = "rawData"

    fun parse(): Either<String, JMHResult> {
        val json = Parser().parse(FileInputStream(inFile))

        if (json is JsonArray<*>) {
            return Either.right(parse(json))
        }

        return Either.left("Invalid json: no array at root")
    }

    private fun parse(arr: JsonArray<*>): JMHResult =
            JMHResult(
                    project = project,
                    commit = commit,
                    trial = trial,
                    benchmarks = arr.mapNotNull { bench ->
                        if (bench is JsonObject) {
                            parseBench(bench)
                        } else {
                            null
                        }
                    }
            )

    private fun parseBench(obj: JsonObject): BenchmarkResult? {
        val v = obj.string(jmhVersion)
        if (v == null) {
            return null
        }
        val n = obj.string(benchmark)
        if (n == null) {
            return null
        }
        val m = obj.string(mode)
        if (m == null) {
            return null
        }
        val t = obj.int(threads)
        if (t == null) {
            return null
        }
        val f = obj.int(forks)
        if (f == null) {
            return null
        }
        val wi = obj.int(warmupIterations)
        if (wi == null) {
            return null
        }
        val wt = obj.string(warmupTime)
        if (wt == null) {
            return null
        }
        val mi = obj.int(measurementIterations)
        if (mi == null) {
            return null
        }
        val mt = obj.string(measurementTime)
        if (mt == null) {
            return null
        }

        val pm = obj.obj(primaryMetric)
        if (pm == null) {
            return null
        }

        val u = pm.string(unit)
        if (u == null) {
            return null
        }

        val vs = pm.array<JsonArray<Float>>(values)
        if (vs == null) {
            return null
        }

        val vals = parseValues(vs)
        if (vals == null) {
            return null
        }


        return BenchmarkResult(
                jmhVersion = v,
                name = n,
                mode = m,
                threads = t,
                forks = f,
                warmupIterations = wi,
                warmuptTime = wt,
                measurementIterations = mi,
                measurementTime = mt,
                unit = u,
                values = vals
        )
    }

    private fun parseValues(arr: JsonArray<JsonArray<Float>>): List<ForkResult>? {
        var valid = true
        val frs = arr.mapIndexed { fork, iters ->
            ForkResult(
                    fork = fork+1,
                    iterations = iters.mapIndexed { iter, value ->
                        IterationResult(
                                iteration = iter+1,
                                value = value
                        )
                    }
            )
        }.toList()
        if (valid) {
            return frs
        }
        return null
    }
}
