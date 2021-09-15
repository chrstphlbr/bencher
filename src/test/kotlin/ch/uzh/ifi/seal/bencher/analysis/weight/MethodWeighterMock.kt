package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.Either

class MethodWeighterMock(private val mw: MethodWeights) : MethodWeighter {
    override fun weights(mapper: MethodWeightMapper): Either<String, MethodWeights> =
            Either.Right(mapper.map(mw))

    companion object {
        fun full(): MethodWeighterMock = MethodWeighterMock(
                mapOf(
                        MethodWeightTestHelper.coreAmWeight,
                        MethodWeightTestHelper.coreBmWeight,
                        MethodWeightTestHelper.coreCmWeight,
                        MethodWeightTestHelper.coreDmWeight,
                        MethodWeightTestHelper.coreEmn1Weight,
                        MethodWeightTestHelper.coreEmn2Weight
                )
        )

        fun empty(): MethodWeighterMock = MethodWeighterMock(mapOf())
    }
}
