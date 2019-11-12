package ch.uzh.ifi.seal.bencher.jmhResults

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import org.apache.logging.log4j.LogManager

class JMHResultIterator(
        private val json: JsonArray<*>,
        private val removeWhitespaceFromJMHParams: Boolean = true
) : Iterator<BenchmarkResult> {

    private val jmhVersion = "jmhVersion"
    private val benchmark = "benchmark"
    private val params = "params"
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
    private val valuesHist = "rawDataHistogram"

    private var current = -1

    override fun next(): BenchmarkResult {
        current += 1

        val el = json[current]
        if (el is JsonObject) {
            return parseBench(el) ?: throw IllegalArgumentException("Element is invalid (at position $current)")
        } else {
            throw IllegalArgumentException("Element is not a json object (at position $current)")
        }
    }

    override fun hasNext(): Boolean = current + 1 < json.size

    private fun parseBench(obj: JsonObject): BenchmarkResult? {
        val v = obj.string(jmhVersion) ?: return null
        val n = obj.string(benchmark) ?: return null
        val m = obj.string(mode) ?: return null
        val t = obj.int(threads) ?: return null
        val f = obj.int(forks) ?: return null
        val wi = obj.int(warmupIterations) ?: return null
        val wt = obj.string(warmupTime) ?: return null
        val mi = obj.int(measurementIterations) ?: return null
        val mt = obj.string(measurementTime) ?: return null

        // jmh params
        val ps: List<Pair<String, String>> = if (obj.containsKey(params)) {
            val po = obj.obj(params)
            // should always be non-null
            if (removeWhitespaceFromJMHParams) {
                po!!.map {
                    val paramName = it.key
                    val paramValue = "${it.value}"
                    val newParamValue = paramValue
                            .replace("\n", "")
                            .replace("\t", "")
                            .replace(" ", "")
                    if (paramValue != newParamValue) {
                        log.info("removed whitespaces for bench '$n' and param '$paramName': orig='$paramValue', new='$newParamValue'")
                    }
                    Pair(paramName, newParamValue)
                }
            } else {
                po!!.map { Pair(it.key, "${it.value}") }
            }
        } else {
            listOf()
        }

        val pm = obj.obj(primaryMetric) ?: return null

        val u = pm.string(unit) ?: return null

        val vals = if (pm.containsKey(values)) {
            val arr = pm.array<JsonArray<Double>>(values) ?: return null
            parseValues(arr)
        } else if (pm.containsKey(valuesHist)) {
            val arr = pm.array<JsonArray<JsonArray<JsonArray<Double>>>>(valuesHist) ?: return null
            parseHistValues(arr)
        } else {
            null
        } ?: return null


        return BenchmarkResult(
                jmhVersion = v,
                name = n,
                jmhParams = ps,
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

    private fun parseValues(arr: JsonArray<JsonArray<Double>>): List<ForkResult> {
        val frs = arr.mapIndexed { fork, iters ->
            ForkResult(
                    fork = fork + 1,
                    iterations = iters.mapIndexed { iter, value ->
                        IterationResult(
                                iteration = iter + 1,
                                invocations = listOf(InvocationResult(
                                        value = value,
                                        count = 1
                                ))
                        )
                    }
            )
        }.toList()
        return frs
    }

    private fun parseHistValues(arr: JsonArray<JsonArray<JsonArray<JsonArray<Double>>>>): List<ForkResult> {
        val frs = arr.mapIndexed { fork, iters ->
            ForkResult(
                    fork = fork + 1,
                    iterations = iters.mapIndexed { iter, values ->
                        IterationResult(
                                iteration = iter + 1,
                                invocations = values.map { value ->
                                    val v = value[0]
                                    val c = value[1]
                                    InvocationResult(value = v, count = c.toInt())
                                }
                        )
                    }
            )
        }.toList()
        return frs
    }

    companion object {
        private val log = LogManager.getLogger(JMHResultIterator::class.java.canonicalName)
    }
}
