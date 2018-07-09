package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Method

data class CGResult(
        val calls: List<CGCall>
)

data class CGCall(
        val from: Method,
        val to: Method,
        val level: Int
)