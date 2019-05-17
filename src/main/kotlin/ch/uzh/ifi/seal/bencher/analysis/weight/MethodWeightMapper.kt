package ch.uzh.ifi.seal.bencher.analysis.weight


interface MethodWeightMapper {
    fun map(mw: MethodWeights): MethodWeights
}

object IdentityMethodWeightMapper : MethodWeightMapper {
    override fun map(mw: MethodWeights): MethodWeights = mw
}

class ValueMethodWeightMapper(private val f: (Double) -> Double) : MethodWeightMapper {
    override fun map(mw: MethodWeights): MethodWeights = mw.mapValues { f(it.value) }
}

// this is actually log10 + 1
val log10MethodWeightMapper = ValueMethodWeightMapper { Math.log10(it) + 1 }
