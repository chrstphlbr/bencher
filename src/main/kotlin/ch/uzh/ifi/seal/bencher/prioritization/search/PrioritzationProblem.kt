package ch.uzh.ifi.seal.bencher.prioritization.search

import arrow.core.Some
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGOverlap
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import ch.uzh.ifi.seal.bencher.measurement.Mean
import ch.uzh.ifi.seal.bencher.measurement.PerformanceChanges
import org.uma.jmetal.problem.permutationproblem.impl.AbstractIntegerPermutationProblem
import org.uma.jmetal.solution.permutationsolution.PermutationSolution


class PrioritizationProblem(
    private val benchmarkIndexMap: BenchmarkIndexMap,
    private val objectives: Set<Objective>,
    coverage: CGResult?, // required for Coverage
    deltaCoverage: CGResult?,  // required for DeltaCoverage
    methodWeights: MethodWeights?,  // required for Coverage and DeltaCoverage
    coverageOverlap: CGOverlap?, // required for CoverageOverlap
    performanceChanges: PerformanceChanges?  // required for ChangeHistory
) : AbstractIntegerPermutationProblem() {

    private val coverage: Map<Method, Double>?
    private val deltaCoverage: Map<Method, Double>?
    private val coverageOverlap: CGOverlap?
    private val performanceChanges: PerformanceChanges?

    init {
        this.coverage = objectives
            .asSequence()
            .filter { it is Coverage }
            .map {
                if (coverage == null) {
                    throw IllegalArgumentException("parameter coverage required for objective Coverage")
                }
                if (methodWeights == null) {
                    throw IllegalArgumentException("parameter methodWeights required for objective Coverage")
                }
                transformCoverage(coverage, methodWeights)
            }
            .firstOrNull()


        this.deltaCoverage = objectives
                .asSequence()
                .filter { it is DeltaCoverage }
                .map {
                    if (deltaCoverage == null) {
                        throw IllegalArgumentException("parameter deltaCoverage required for objective DeltaCoverage")
                    }
                    if (methodWeights == null) {
                        throw IllegalArgumentException("parameter methodWeights required for objective DeltaCoverage")
                    }
                    transformCoverage(deltaCoverage, methodWeights)
                }
                .firstOrNull()

        this.coverageOverlap = objectives
            .asSequence()
            .filter { it is CoverageOverlap }
            .map {
                if (coverageOverlap == null) {
                    throw IllegalArgumentException("parameter coverageOverlap required for objective CoverageOverlap")
                }
                coverageOverlap
            }
            .firstOrNull()

        this.performanceChanges = objectives.
            asSequence()
            .filter { it is ChangeHistory }
            .map {
                if (performanceChanges == null) {
                    throw IllegalArgumentException("parameter performanceChanges required for objective ChangeHistory")
                }
                performanceChanges
            }
            .firstOrNull()


        // setup problem

        name = problemName
        numberOfObjectives = objectives.size
        numberOfVariables = this.benchmarkIndexMap.size
    }

    private fun transformCoverage(cov: CGResult, methodWeights: MethodWeights): Map<Method, Double> =
        cov.calls.mapValues { (_, rs) ->
            rs
                .all(true)
                .map { methodWeights.getOrDefault(it.unit, 1.0) }
                .fold(0.0) { acc, d -> acc + d }
        }

    override fun evaluate(solution: PermutationSolution<Int>): PermutationSolution<Int> {
        val individualObjectives = (0 until solution.length)
            .asSequence()
            .map { i ->
                val benchId = solution.variables()[i]
                val bench = benchmarkIndexMap[benchId] ?: throw IllegalStateException("no benchmark for index $benchId")
                bench as? Benchmark ?: throw IllegalStateException("method not a benchmark: $bench")
            }
            .map { objectives(it) }
            .toList()

        val os = averagePercentageObjectives(individualObjectives)


        objectives.forEachIndexed { i, o ->
            val ov = when (o) {
                is Coverage -> os.coverage
                is DeltaCoverage -> os.deltaCoverage
                is CoverageOverlap -> os.overlappingPercentage
                is ChangeHistory -> os.averageHistoricalPerformanceChange
            }

            solution.objectives()[i] = o.toMinimization(ov)
        }

        return solution
    }

    private fun averagePercentageObjectives(objs: List<Objectives>): Objectives {
        val perObjectives = objs
            .fold(ObjectivesList()) { acc, obj ->
                acc.coverage.add(obj.coverage)
                acc.deltaCoverage.add(obj.deltaCoverage)
                acc.overlappingPercentage.add(obj.overlappingPercentage)
                acc.averageHistoricalPerformanceChange.add(obj.averageHistoricalPerformanceChange)
                acc
            }

        return Objectives(
            bench = allBenchs,
            coverage = averagePercentageCoverage(perObjectives.coverage),
            deltaCoverage = averagePercentageCoverage(perObjectives.deltaCoverage),
            overlappingPercentage = averagePercentageOverlap(perObjectives.overlappingPercentage),
            averageHistoricalPerformanceChange = averagePercentagePerformanceChange(perObjectives.averageHistoricalPerformanceChange)
        )
    }

    private fun averagePercentageCoverage(l: List<Double>): Double = averagePercentage(l)

    private fun averagePercentageOverlap(l: List<Double>): Double = averagePercentage(l, defaultEmptyList = 1.0, defaultListSumZero = 2.0)

    private fun averagePercentagePerformanceChange(l: List<Double>): Double = averagePercentage(l)

    private fun objectives(b: Benchmark): Objectives {
        val cov = if (coverage != null) {
            coverage[b] ?: throw IllegalStateException("no coverage for $b")
        } else {
            Coverage.startValue
        }

        val op = if (coverageOverlap != null) {
            coverageOverlap.overlappingPercentage(b)
        } else {
            CoverageOverlap.startValue
        }

        val ch = if (performanceChanges != null) {
            performanceChanges.benchmarkChangeStatistic(b, Mean, Some(0.0)).getOrElse {
                throw IllegalStateException("no benchmark change statistic for $b")
            }
        } else {
            ChangeHistory.startValue
        }

        val dcov = if (deltaCoverage != null) {
            deltaCoverage[b] ?: throw IllegalStateException("no delta coverage for $b")
        } else {
            DeltaCoverage.startValue
        }

        return Objectives(
            bench = b,
            coverage = cov,
            deltaCoverage = dcov,
            overlappingPercentage = op,
            averageHistoricalPerformanceChange = ch
        )
    }

    override fun getLength(): Int = numberOfVariables

    companion object {
        private const val problemName = "Prioritization"

        private val allBenchs = Benchmark(
            clazz = "ALL",
            name = "BENCHS",
            params = listOf(),
            jmhParams = listOf(),
            returnType = "",
        )

        private data class Objectives(
            val bench: Benchmark,
            val coverage: Double,
            val deltaCoverage: Double,
            val overlappingPercentage: Double,
            val averageHistoricalPerformanceChange: Double
        )

        private data class ObjectivesList(
            val coverage: MutableList<Double> = mutableListOf(),
            val deltaCoverage: MutableList<Double> = mutableListOf(),
            val overlappingPercentage: MutableList<Double> = mutableListOf(),
            val averageHistoricalPerformanceChange: MutableList<Double> = mutableListOf()
        )
    }
}
