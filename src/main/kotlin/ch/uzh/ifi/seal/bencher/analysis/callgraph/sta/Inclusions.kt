package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.Method


sealed class WalaSCGInclusions {
    abstract fun include(m: Method): Boolean
}
object IncludeAll : WalaSCGInclusions() {
    override fun toString(): String = "IncludeAll"
    override fun include(m: Method): Boolean = true
}
data class IncludeOnly(
        val includes: Set<String>
) : WalaSCGInclusions() {
    override fun toString(): String = includes.joinToString(separator = ",", prefix = "IncludeOnly[", postfix = "]")
    override fun include(m: Method): Boolean = includes.any { m.clazz.startsWith(it) }
}
