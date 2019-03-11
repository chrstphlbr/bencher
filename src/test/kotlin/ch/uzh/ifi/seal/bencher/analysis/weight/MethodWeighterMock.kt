package ch.uzh.ifi.seal.bencher.analysis.weight

import org.funktionale.either.Either

class MethodWeighterMock(private val mw: MethodWeights): MethodWeighter{
    override fun weights(): Either<String, MethodWeights> =
            Either.right(mw)

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
