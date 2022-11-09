package ch.uzh.ifi.seal.bencher.prioritization.greedy

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.weight.CoverageUnitWeightMapper
import ch.uzh.ifi.seal.bencher.analysis.weight.CoverageUnitWeights
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import ch.uzh.ifi.seal.bencher.prioritization.Prioritizer
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizerTestHelper

class TotalPrioritizerTest : GreedyPrioritizerTest() {

    override fun prioritizer(cov: Coverages, coverageUnitWeights: CoverageUnitWeights, coverageUnitWeightMapper: CoverageUnitWeightMapper): Prioritizer =
            TotalPrioritizer(coverages = cov, coverageUnitWeights = coverageUnitWeightMapper.map(coverageUnitWeights))

    private fun ebs(param: Boolean, ebs: List<Pair<Benchmark, Double>>): List<PrioritizerTestHelper.ExpectedPrioBench> =
            if (param) {
                var currRank = 1
                ebs.map { (b, v) ->
                    val pbs = b.parameterizedBenchmarks()
                    val mpbs = pbs.map {
                        PrioritizerTestHelper.ExpectedPrioBench(
                            benchmark = it,
                            value = v,
                            rank = currRank
                        )
                    }
                    currRank += pbs.size
                    mpbs
                }.flatten()
            } else {
                ebs.mapIndexed { i, (b, v) ->
                    PrioritizerTestHelper.ExpectedPrioBench(
                        benchmark = b,
                        value = v,
                        rank = i + 1
                    )
                }
            }

    override fun assertionsBenchsNotInCoverages(param: Boolean, bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        val eBenchmarks = ebs(param, listOf(
                Pair(JarTestHelper.BenchParameterized.bench1, mf(5.75)),
                Pair(JarTestHelper.BenchNonParameterized.bench2, mf(3.0))
        ))

        PrioritizerTestHelper.assertBenchmarks(eBenchmarks, bs)
    }

    override fun assertionsWithPrios(param: Boolean, bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        val ebs = ebs(param, listOf(
                Pair(JarTestHelper.BenchParameterized.bench1, mf(5.75)),
                Pair(JarTestHelper.OtherBench.bench3, mf(5.0)),
                Pair(JarTestHelper.BenchNonParameterized.bench2, mf(3.0)),
                Pair(JarTestHelper.BenchParameterized2.bench4, mf(2.5))
        ))

        PrioritizerTestHelper.assertBenchmarks(ebs, bs)
    }

    override fun assertionsWithPriosDifferentWeights(param: Boolean, bs: List<PrioritizedMethod<Benchmark>>, mf: (Double) -> Double) {
        val ebs = ebs(param, listOf(
                Pair(JarTestHelper.BenchParameterized2.bench4, mf(5.5)),
                Pair(JarTestHelper.BenchParameterized.bench1, mf(4.75)),
                Pair(JarTestHelper.OtherBench.bench3, mf(4.0)),
                Pair(JarTestHelper.BenchNonParameterized.bench2, mf(3.0))
        ))

        PrioritizerTestHelper.assertBenchmarks(ebs, bs)
    }
}
