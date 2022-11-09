package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages


interface CoverageUnitWeighter {
    fun weights(mapper: CoverageUnitWeightMapper): Either<String, CoverageUnitWeights>

    fun weights(): Either<String, CoverageUnitWeights> = weights(IdentityMethodWeightMapper)
}

class CoveragesWeighter(private val cov: Coverages) : CoverageUnitWeighter {
    private val parsed = mutableMapOf<CoverageUnitWeightMapper, CoverageUnitWeights>()

    override fun weights(mapper: CoverageUnitWeightMapper): Either<String, CoverageUnitWeights> {
        val w = parsed[mapper]
        return if (w == null) {
            val mw = cov.all(true).associate { Pair(it.unit, 1.0) }
            val mmw = mapper.map(mw)
            parsed[mapper] = mmw
            Either.Right(mmw)
        } else {
            Either.Right(w)
        }
    }
}
