package ch.uzh.ifi.seal.bencher

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

data class Class(
        val name: String
)

interface MethodFactory {
    fun plainMethod(clazz: String, name: String, params: List<String>): PlainMethod
    fun benchmark(clazz: String, name: String, params: List<String>, jmhParams: JmhParameters): Benchmark
    fun setupMethod(clazz: String, name: String, params: List<String>): SetupMethod
    fun tearDownMethod(clazz: String, name: String, params: List<String>): TearDownMethod
}

object MF : MethodFactory {

    private val s = mutableSetOf<PlainMethod>()
    private val l = ReentrantReadWriteLock()

    override fun plainMethod(clazz: String, name: String, params: List<String>): PlainMethod =
            l.write {
                val pm = PlainMethod(
                        clazz = clazz,
                        name = name,
                        params = params
                )

                val fpm = s.findLast { it == pm }
                return if (fpm == null) {
                    s.add(pm)
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
                jmhParams = jmhParams
        )
    }

    override fun setupMethod(clazz: String, name: String, params: List<String>): SetupMethod {
        return SetupMethod(
                clazz = clazz,
                name = name,
                params = params
        )
    }

    override fun tearDownMethod(clazz: String, name: String, params: List<String>): TearDownMethod {
        return TearDownMethod(
                clazz = clazz,
                name = name,
                params = params
        )
    }
}

sealed class Method(
        open val clazz: String,
        open val name: String,
        open val params: List<String>
) {
    fun toPlainMethod(): PlainMethod = MF.plainMethod(
            clazz = this.clazz,
            name = this.name,
            params = this.params
    )
}

object NoMethod : Method("", "", listOf())

data class PlainMethod(
        override val clazz: String,
        override val name: String,
        override val params: List<String>
) : Method(clazz, name, params)

typealias JmhParameters = List<Pair<String, String>>

data class Benchmark(
        override val clazz: String,
        override val name: String,
        override val params: List<String>,
        val jmhParams: JmhParameters
) : Method(clazz, name, params)

data class SetupMethod(
    override val clazz: String,
    override val name: String,
    override val params: List<String>
) : Method(clazz, name, params)

data class TearDownMethod(
        override val clazz: String,
        override val name: String,
        override val params: List<String>
) : Method(clazz, name, params)

fun Collection<Benchmark>.benchmarksFor(className: String, methodName: String): Collection<Benchmark> =
        this.filter {
            it.clazz == className && it.name == methodName
        }
