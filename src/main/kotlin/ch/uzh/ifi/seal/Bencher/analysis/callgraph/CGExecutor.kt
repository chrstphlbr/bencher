package ch.uzh.ifi.seal.bencher.analysis.callgraph

interface CGExecutor {
    fun get(): CGResult
}