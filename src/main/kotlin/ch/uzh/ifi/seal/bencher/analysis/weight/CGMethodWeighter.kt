package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import org.funktionale.either.Either

class CGMethodWeighter(private val cg: CGResult) : MethodWeighter {
    private val parsed = mutableMapOf<MethodWeightMapper, MethodWeights>()

    override fun weights(mapper: MethodWeightMapper): Either<String, MethodWeights> {
        val w = parsed[mapper]
        return if (w == null) {
            val mw = cg.reachabilities(true).associate { Pair(it.to, 1.0) }
            val mmw = mapper.map(mw)
            parsed[mapper] = mmw
            Either.right(mmw)
        } else {
            Either.right(w)
        }
    }
}
