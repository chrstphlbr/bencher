package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.MF
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.*
import org.openjdk.jmh.annotations.*
import kotlin.random.Random

@State(Scope.Benchmark)
open class CoverageUnitWeightBench {

    private val maxTos = 10000
    private val start = MF.benchmark(
            clazz = "c",
            name = "b",
            params = listOf("org.openjdk.jmh.infra.Blackhole"),
            jmhParams = listOf()
    )
    private val acc: (Double, Double) -> Double = Double::plus

    @Param(value = ["100", "1000", "10000"])
    var nrCoverages: Int = 10000

    @Param(value = ["100", "1000", "10000"])
    var nrMethodWeights: Int = 10000

    @Param(value = ["10", "100", "1000"])
    var nrExclusions: Int = 1000

    private var cus: List<CoverageUnit> = (1..maxTos).map { i ->
        CoverageUnitMethod(
            MF.plainMethod(
                clazz = "c$i",
                name = "m$i",
                params = listOf("a", "b", "c")
            )
        )
    }
    private lateinit var cus2: List<CoverageUnit>

    private lateinit var cov: CoverageComputation
    private lateinit var cuws: CoverageUnitWeights
    private lateinit var exs: Set<CoverageUnit>

    @Setup(Level.Trial)
    fun setupTrial() {
        cus2 = cus.take(nrCoverages)

        cov = Coverage(
                of = start,
                unitResults = cus2
                        .map { to ->
                            CUF.covered(
                                    of = start,
                                    unit = to,
                                    level = 2
                            )
                        }
                        .toSet()
        )

        cuws = cus.asSequence()
            .take(nrMethodWeights)
            .associateWith { 1.0 }
    }

    @Setup(Level.Iteration)
    fun setupIter() {
        val cuexs = mutableSetOf<CoverageUnit>()
        while (cuexs.size < nrExclusions) {
            val idx = Random.nextInt(0, maxTos)
            val to = cus[idx]
            if (!cuexs.contains(to)) {
                cuexs.add(to)
            }
        }
        exs = cuexs
    }

    @Benchmark
    fun coverageUnitWeightsFirst(): Pair<Double, Set<CoverageUnit>> {
        return cuwUnitWeightsFirst(
                method = start,
                coverage = cov,
                coverageUnitWeights = cuws,
                exclusions = exs,
                accumulator = acc
        )
    }

    @Benchmark
    fun coverageUnitsFirst(): Pair<Double, Set<CoverageUnit>> {
        return cuwCoveredUnitsFirst(
                method = start,
                coverage = cov,
                coverageUnitWeights = cuws,
                exclusions = exs,
                accumulator = acc
        )
    }
}
