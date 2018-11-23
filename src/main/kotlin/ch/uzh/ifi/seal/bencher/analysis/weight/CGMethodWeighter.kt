package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import org.funktionale.either.Either

class CGMethodWeighter(private val cg: CGResult) : MethodWeighter {
    private lateinit var mw: MethodWeights
    private var parsed: Boolean = false

    override fun weights(): Either<String, MethodWeights> =
            if (parsed) {
                Either.right(mw)
            } else {
                mw = cg.benchCalls.flatMap { it.value }.map { Pair(it.method, 1.0) }.toMap()
                parsed = true
                Either.right(mw)
            }
}
