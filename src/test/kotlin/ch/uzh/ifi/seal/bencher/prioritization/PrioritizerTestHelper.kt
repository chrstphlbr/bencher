package ch.uzh.ifi.seal.bencher.prioritization

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoveragesTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.toCoverageUnit
import ch.uzh.ifi.seal.bencher.analysis.weight.CoverageUnitWeights
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeightTestHelper
import org.junit.jupiter.api.Assertions

object PrioritizerTestHelper {

    val benchs = listOf(
            JarTestHelper.BenchParameterized.bench1,
            JarTestHelper.BenchNonParameterized.bench2,
            JarTestHelper.OtherBench.bench3,
            JarTestHelper.BenchParameterized2.bench4
    )

    val covFull = Coverages(mapOf(CoveragesTestHelper.b1MethodCov, CoveragesTestHelper.b2MethodCov, CoveragesTestHelper.b3MethodCov, CoveragesTestHelper.b4MethodCov))
    val covTwo = Coverages(mapOf(CoveragesTestHelper.b1MethodCov, CoveragesTestHelper.b2MethodCov))

    val mwFull: CoverageUnitWeights =
        mapOf(
            MethodWeightTestHelper.coreAmWeight,
            MethodWeightTestHelper.coreBmWeight,
            MethodWeightTestHelper.coreCmWeight,
            MethodWeightTestHelper.coreDmWeight,
            MethodWeightTestHelper.coreEmn1Weight,
            MethodWeightTestHelper.coreEmn2Weight
        )
            .mapKeys { (k, _) -> k.toCoverageUnit() }


    val mwEmpty: CoverageUnitWeights = mapOf()

    fun assertPriority(prioritizedMethod: PrioritizedMethod<out Method>, rank: Int, total: Int, value: PriorityValue) {
        val r = prioritizedMethod.priority.rank
        Assertions.assertEquals(rank, r, "${prioritizedMethod.method} has unexpected rank")
        val t = prioritizedMethod.priority.total
        Assertions.assertEquals(total, t, "${prioritizedMethod.method} has unexpected total")
        val v = prioritizedMethod.priority.value
        Assertions.assertEquals(value, v, "${prioritizedMethod.method} has unexpected value")
    }

    fun assertBenchmark(prioritizedBench: PrioritizedMethod<Benchmark>, expectedBench: Benchmark, rank: Int, total: Int, value: PriorityValue) {
        Assertions.assertEquals(expectedBench, prioritizedBench.method, "Benchmark not as expected")
        assertPriority(
                prioritizedMethod = prioritizedBench,
                rank = rank,
                total = total,
                value = value
        )
    }

    fun assertEqualRankBenchmarks(eBenchmarks: List<Benchmark>, pBenchmarks: List<PrioritizedMethod<Benchmark>>, rank: Int, total: Int, value: Double) {
        val eSize = eBenchmarks.size
        Assertions.assertEquals(eSize, pBenchmarks.size)

        // check rank, total, and value
        pBenchmarks.forEach { assertPriority(it, rank, total, PrioritySingle(value)) }

        val benchCartProd: List<List<Pair<Benchmark, Benchmark>>> = eBenchmarks.map { eb ->
            pBenchmarks.map { pb ->
                Pair(eb, pb.method)
            }
        }

        val valid = benchCartProd.fold(true) { acco, elo ->
            acco && elo.fold(false) { acci, eli ->
                acci || eli.first == eli.second
            }
        }

        Assertions.assertTrue(valid, "Invalid equal rank benchmarks")
    }

    data class ExpectedPrioBench(
            val benchmark: Benchmark,
            val rank: Int,
            val value: Double
    )

    fun assertBenchmarks(eBenchmarks: List<ExpectedPrioBench>, pBenchmarks: List<PrioritizedMethod<Benchmark>>, total: Int? = null) {
        Assertions.assertEquals(eBenchmarks.size, pBenchmarks.size, "Prioritized benchmark list size unexpected")
        val eTotal = total ?: eBenchmarks.size

        pBenchmarks.forEachIndexed { i, pb ->
            val eb = eBenchmarks[i]
            assertBenchmark(
                    prioritizedBench = pb,
                    expectedBench = eb.benchmark,
                    rank = eb.rank,
                    total = eTotal,
                    value = PrioritySingle(eb.value)
            )
        }
    }
}
