package ch.uzh.ifi.seal.Bencher.callgraph

import ch.uzh.ifi.seal.Bencher.Benchmark
import ch.uzh.ifi.seal.Bencher.Method

data class CGResult(
        val calls: List<CGCall>
)

data class CGCall(
        val benchmark: Benchmark,
        val method: Method,
        val level: Int
)