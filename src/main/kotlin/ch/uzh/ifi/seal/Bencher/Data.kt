package ch.uzh.ifi.seal.Bencher

data class Benchmark(
        val clazz: String,
        val name: String,
        val params: List<String>
)

data class Method(
        val clazz: String,
        val name: String,
        val params: List<String>
)