package ch.uzh.ifi.seal.bencher

data class Class(
        val file: String,
        val name: String
)

sealed class Method(
        open val clazz: String,
        open val name: String,
        open val params: List<String>
)

data class PlainMethod(
        override val clazz: String,
        override val name: String,
        override val params: List<String>
) : Method(clazz, name, params)

data class PossibleMethod(
        override val clazz: String,
        override val name: String,
        override val params: List<String>,
        val nrPossibleTargets: Int,
        val idPossibleTargets: Int
) : Method(clazz, name, params)

data class Benchmark(
        override val clazz: String,
        override val name: String,
        override val params: List<String>,
        val jmhParams: List<Pair<String, String>>
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
