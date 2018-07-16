package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import com.ibm.wala.ipa.callgraph.CallGraph

typealias BenchmarkCalls = Map<Benchmark, Iterable<MethodCall>>

data class MethodCall(
        val method: Method,
        val level: Int
)

sealed class CGResult(
        open val benchCalls: BenchmarkCalls,
        open val toolCg: Any
)

data class WalaCGResult(
        override val benchCalls: BenchmarkCalls,
        override val toolCg: CallGraph
) : CGResult(benchCalls, toolCg)
