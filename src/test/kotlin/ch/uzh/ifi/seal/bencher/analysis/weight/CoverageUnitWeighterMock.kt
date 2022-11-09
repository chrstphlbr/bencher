package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.toCoverageUnit

class CoverageUnitWeighterMock(private val mw: CoverageUnitWeights) : CoverageUnitWeighter {
    override fun weights(mapper: CoverageUnitWeightMapper): Either<String, CoverageUnitWeights> =
            Either.Right(mapper.map(mw))

    companion object {
        fun full(): CoverageUnitWeighterMock = CoverageUnitWeighterMock(
                mapOf(
                        MethodWeightTestHelper.coreAmWeight,
                        MethodWeightTestHelper.coreBmWeight,
                        MethodWeightTestHelper.coreCmWeight,
                        MethodWeightTestHelper.coreDmWeight,
                        MethodWeightTestHelper.coreEmn1Weight,
                        MethodWeightTestHelper.coreEmn2Weight
                )
                    .mapKeys { (k, _) -> k.toCoverageUnit() }
        )

        fun empty(): CoverageUnitWeighterMock = CoverageUnitWeighterMock(mapOf())
    }
}
