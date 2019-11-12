package ch.uzh.ifi.seal.bencher.analysis.finder.shared

data class BenchField(
        var isParam: Boolean = false,
        var jmhParams: MutableMap<String, MutableList<String>> = mutableMapOf()
)