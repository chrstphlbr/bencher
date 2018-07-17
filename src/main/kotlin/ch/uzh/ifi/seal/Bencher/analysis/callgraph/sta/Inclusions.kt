package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

sealed class WalaSCGInclusions
object IncludeAll : WalaSCGInclusions()
data class IncludeOnly(
        val includes: Set<String>
) : WalaSCGInclusions()