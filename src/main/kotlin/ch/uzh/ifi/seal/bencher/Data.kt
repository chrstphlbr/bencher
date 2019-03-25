package ch.uzh.ifi.seal.bencher

data class Class(
        val name: String
)

sealed class Method(
        open val clazz: String,
        open val name: String,
        open val params: List<String>
) {
    fun toPlainMethod(): PlainMethod = PlainMethod(
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
