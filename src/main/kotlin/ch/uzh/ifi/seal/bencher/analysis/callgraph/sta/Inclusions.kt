package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

sealed class WalaSCGInclusions
object IncludeAll : WalaSCGInclusions() {
    override fun toString(): String = "IncludeAll"
}
data class IncludeOnly(
        val includes: Set<String>
) : WalaSCGInclusions() {
    override fun toString(): String = includes.joinToString(separator = ",", prefix = "IncludeOnly[", postfix = "]")
}
