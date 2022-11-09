package ch.uzh.ifi.seal.bencher.analysis.weight

import kotlin.math.log10


interface CoverageUnitWeightMapper {
    fun map(cuw: CoverageUnitWeights): CoverageUnitWeights
}

object IdentityMethodWeightMapper : CoverageUnitWeightMapper {
    override fun map(cuw: CoverageUnitWeights): CoverageUnitWeights = cuw
}

class ValueMethodWeightMapper(private val f: (Double) -> Double) : CoverageUnitWeightMapper {
    override fun map(cuw: CoverageUnitWeights): CoverageUnitWeights = cuw.mapValues { f(it.value) }
}

// this is actually log10 + 1
val log10CoverageUnitWeightMapper = ValueMethodWeightMapper { log10(it) + 1 }
