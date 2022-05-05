package ch.uzh.ifi.seal.bencher.analysis.coverage

import ch.uzh.ifi.seal.bencher.Method


sealed class CoverageInclusions {
    abstract fun include(m: Method): Boolean
}

object IncludeAll : CoverageInclusions() {
    override fun toString(): String = "IncludeAll"
    override fun include(m: Method): Boolean = true
}

data class IncludeOnly(
        val includes: Set<String>
) : CoverageInclusions() {
    override fun toString(): String = includes.joinToString(separator = ",", prefix = "IncludeOnly[", postfix = "]")
    override fun include(m: Method): Boolean = includes.any { m.clazz.startsWith(it) }
}
