package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult

class CGMethodWeighter(private val cg: CGResult) : MethodWeighter {
    private val parsed = mutableMapOf<MethodWeightMapper, MethodWeights>()

    override fun weights(mapper: MethodWeightMapper): Either<String, MethodWeights> {
        val w = parsed[mapper]
        return if (w == null) {
            val mw = cg.all(true).associate { Pair(it.unit, 1.0) }
            val mmw = mapper.map(mw)
            parsed[mapper] = mmw
            Either.Right(mmw)
        } else {
            Either.Right(w)
        }
    }
}
