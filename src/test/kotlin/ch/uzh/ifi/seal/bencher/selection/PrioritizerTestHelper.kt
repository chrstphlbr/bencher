package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGTestHelper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightTestHelper
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights

object PrioritizerTestHelper {

    val benchs = listOf(
            JarTestHelper.BenchParameterized.bench1,
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.OtherBench.bench3,
            JarTestHelper.BenchParameterized2.bench4
    )

    val cgFull = CGResult(mapOf(CGTestHelper.b1Cg, CGTestHelper.b2Cg, CGTestHelper.b3Cg, CGTestHelper.b4Cg))
    val cgTwo = CGResult(mapOf(CGTestHelper.b1Cg, CGTestHelper.b2Cg))

    val mwFull: MethodWeights = mapOf(
            MethodWeightTestHelper.coreAmWeight,
            MethodWeightTestHelper.coreBmWeight,
            MethodWeightTestHelper.coreCmWeight,
            MethodWeightTestHelper.coreDmWeight
    )

    val mwEmpty: MethodWeights = mapOf()
}
