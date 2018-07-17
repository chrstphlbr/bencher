package ch.uzh.ifi.seal.bencher

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
