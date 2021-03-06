package ch.uzh.ifi.seal.bencher

import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

data class Class(
        val name: String
)

object ID {
    fun string(from: Method, to: Method, level: Int = -1, probability: Double = -1.0): String =
            "${string(from)}-$level-$probability->${string(to)}"

    fun string(m: Method): String = "$m"

    fun string(clazz: String, name: String, params: List<String>, jmhParams: JmhParameters = listOf()): String =
            "$clazz.$name($params)($jmhParams)"
}

interface MethodFactory {
    fun plainMethod(clazz: String, name: String, params: List<String>): PlainMethod
    fun plainMethod(clazz: String, name: String, params: List<String>, returnType: String): PlainMethod
    fun benchmark(clazz: String, name: String, params: List<String>, jmhParams: JmhParameters): Benchmark
    fun benchmark(clazz: String, name: String, params: List<String>, returnType: String, jmhParams: JmhParameters, group: String?): Benchmark
    fun setupMethod(clazz: String, name: String, params: List<String>): SetupMethod
    fun setupMethod(clazz: String, name: String, params: List<String>, returnType: String): SetupMethod
    fun tearDownMethod(clazz: String, name: String, params: List<String>): TearDownMethod
    fun tearDownMethod(clazz: String, name: String, params: List<String>, returnType: String): TearDownMethod
}

object MF : MethodFactory {

    private val s = mutableMapOf<String, PlainMethod>()
    private val l = ReentrantReadWriteLock()

    override fun plainMethod(clazz: String, name: String, params: List<String>): PlainMethod {
        return plainMethod(clazz, name, params, SourceCodeConstants.void)
    }

    override fun plainMethod(clazz: String, name: String, params: List<String>, returnType: String): PlainMethod =
            l.write {
                val id = ID.string(clazz, name, params)
                val fpm = s[id]
                return if (fpm == null) {
                    val pm = PlainMethod(
                            clazz = clazz,
                            name = name,
                            params = params,
                            returnType = returnType
                    )
                    s[id] = pm
                    pm
                } else {
                    fpm
                }
            }

    override fun benchmark(clazz: String, name: String, params: List<String>, jmhParams: JmhParameters): Benchmark {
        return Benchmark(
                clazz = clazz,
                name = name,
                params = params,
                returnType = SourceCodeConstants.void,
                jmhParams = jmhParams,
                group = null
        )
    }

    override fun benchmark(clazz: String, name: String, params: List<String>, returnType: String, jmhParams: JmhParameters, group: String?): Benchmark {
        return Benchmark(
                clazz = clazz,
                name = name,
                params = params,
                returnType = returnType,
                jmhParams = jmhParams,
                group = group
        )
    }

    override fun setupMethod(clazz: String, name: String, params: List<String>): SetupMethod {
        return SetupMethod(
                clazz = clazz,
                name = name,
                params = params,
                returnType = SourceCodeConstants.void
        )
    }

    override fun setupMethod(clazz: String, name: String, params: List<String>, returnType: String): SetupMethod {
        return SetupMethod(
                clazz = clazz,
                name = name,
                params = params,
                returnType = returnType
        )
    }

    override fun tearDownMethod(clazz: String, name: String, params: List<String>): TearDownMethod {
        return TearDownMethod(
                clazz = clazz,
                name = name,
                params = params,
                returnType = SourceCodeConstants.void
        )
    }

    override fun tearDownMethod(clazz: String, name: String, params: List<String>, returnType: String): TearDownMethod {
        return TearDownMethod(
                clazz = clazz,
                name = name,
                params = params,
                returnType = returnType
        )
    }
}

sealed class Method(
        open val clazz: String,
        open val name: String,
        open val params: List<String>,
        open val returnType: String
) {
    fun toPlainMethod(): PlainMethod = MF.plainMethod(
            clazz = this.clazz,
            name = this.name,
            params = this.params,
            returnType = this.returnType
    )
}

object NoMethod : Method("", "", listOf(), "")

data class PlainMethod(
        override val clazz: String,
        override val name: String,
        override val params: List<String>,
        override val returnType: String
) : Method(clazz, name, params, returnType)

typealias JmhParameters = List<Pair<String, String>>

data class Benchmark(
        override val clazz: String,
        override val name: String,
        override val params: List<String>,
        override val returnType: String,
        val jmhParams: JmhParameters,
        val group: String? = null
) : Method(clazz, name, params, returnType) {
    fun parameterizedBenchmarks(reversed: Boolean = false): List<Benchmark> =
            if (jmhParams.isEmpty()) {
                listOf(this)
            } else {
                val paramsMap = jmhParamsMap()
                val nps = if (reversed) {
                    jmhParamsLists(paramsMap).reversed()
                } else {
                    jmhParamsLists(paramsMap)
                }

                nps.map {
                    MF.benchmark(
                            clazz = this.clazz,
                            name = this.name,
                            params = this.params,
                            returnType = this.returnType,
                            jmhParams = it,
                            group = this.group
                    )
                }
            }

    private fun jmhParamsMap(): TreeMap<String, List<String>> {
        val m = TreeMap<String, List<String>>()
        jmhParams.forEach { (k, v) ->
            if (m.containsKey(k)) {
                m[k] = m[k]!! + v
            } else {
                m[k] = listOf(v)
            }
        }
        return m
    }

    private fun jmhParamsLists(m: TreeMap<String, List<String>>): List<JmhParameters> {
        val e = m.pollFirstEntry() ?: return listOf()
        val others = jmhParamsLists(m)

        return if (others.isEmpty()) {
            e.value.map { v -> jmhParam(e.key, v) }
        } else {
            e.value.flatMap { v ->
                others.map { jmhParams ->
                    jmhParam(e.key, v) + jmhParams
                }
            }
        }
    }

    private fun jmhParam(k: String, v: String): JmhParameters = listOf(Pair(k, v))
}

data class SetupMethod(
        override val clazz: String,
        override val name: String,
        override val params: List<String>,
        override val returnType: String
) : Method(clazz, name, params, returnType)

data class TearDownMethod(
        override val clazz: String,
        override val name: String,
        override val params: List<String>,
        override val returnType: String
) : Method(clazz, name, params, returnType)

fun Collection<Benchmark>.benchmarksFor(className: String, methodName: String): Collection<Benchmark> =
        this.filter {
            it.clazz == className.replaceSlashesWithDots && it.name == methodName
        }

fun Iterable<Benchmark>.parameterizedBenchmarks(reversed: Boolean = false): List<Benchmark> =
        this.flatMap { it.parameterizedBenchmarks(reversed) }
