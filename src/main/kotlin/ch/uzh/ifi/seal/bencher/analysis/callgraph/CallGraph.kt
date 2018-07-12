package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Method
import com.ibm.wala.ipa.callgraph.CallGraph as WalaCallGraph

data class CallGraph(
        val calls: List<CGCall>
)

data class CGCall(
        val from: Method,
        val to: Method,
        val level: Int
)

sealed class CGResult(
        open val cg: CallGraph,
        open val toolCg: Any
)

data class WalaCGResult(
        override val cg: CallGraph,
        override val toolCg: WalaCallGraph
) : CGResult(cg, toolCg)
