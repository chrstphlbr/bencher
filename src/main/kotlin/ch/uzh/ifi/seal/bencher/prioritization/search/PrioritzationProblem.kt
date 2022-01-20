package ch.uzh.ifi.seal.bencher.prioritization.search

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
    cgResult: CGResult,
    methodWeights: MethodWeights,
    private val cgOverlap: CGOverlap,
    private val performanceChanges: PerformanceChanges,
    benchmarkIndexMap: BenchmarkIndexMap? = null
) : AbstractIntegerPermutationProblem() {

    private val nrBenchmarks = cgResult.calls.size
    private val benchmarkIndexMap: BenchmarkIndexMap
    private val coverage: Map<Method, Double>

    init {
        this.benchmarkIndexMap = benchmarkIndexMap ?: BenchmarkIndexMapImpl(
            cgResult.calls.keys.map { it as Benchmark }
        )

        coverage = cgResult.calls.mapValues { (_, rs) ->
            rs
                .reachabilities(true)
                .map { methodWeights.getOrDefault(it.to, 1.0) }
                .fold(0.0) { acc, d -> acc + d }
        }

        // setup problem

        name = problemName
        numberOfObjectives = nrObjectives
        numberOfVariables = this.benchmarkIndexMap.size
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

        val objectives = averagePercentageObjectives(individualObjectives)

        solution.objectives()[0] = objectives.coverage
        solution.objectives()[1] = objectives.overlappingPercentage
        solution.objectives()[2] = objectives.averageHistoricalPerformanceChange

        return solution
    }

    private fun averagePercentageObjectives(objs: List<Objectives>): Objectives {
        val perObjectives = objs
            .fold(Triple(mutableListOf<Double>(), mutableListOf<Double>(), mutableListOf<Double>())) { acc, obj ->
                acc.first.add(obj.coverage)
                acc.second.add(obj.overlappingPercentage)
                acc.third.add(obj.averageHistoricalPerformanceChange)
                acc
            }

        return Objectives(
            bench = allBenchs,
            coverage = averagePercentageCoverage(perObjectives.first),
            overlappingPercentage = averagePercentageOverlap(perObjectives.second),
            averageHistoricalPerformanceChange = averagePercentagePerformanceChange(perObjectives.third)
        )
    }

    private fun averagePercentageCoverage(l: List<Double>): Double = averagePercentage(l)

    private fun averagePercentageOverlap(l: List<Double>): Double = averagePercentage(l)

    private fun averagePercentagePerformanceChange(l: List<Double>): Double = averagePercentage(l)

    private fun objectives(b: Benchmark): Objectives =
        Objectives(
            bench = b,
            coverage = coverage[b] ?: throw IllegalStateException("no coverage for $b"),
            overlappingPercentage = cgOverlap.overlappingPercentage(b),
            averageHistoricalPerformanceChange = performanceChanges.benchmarkChangeStatistic(b, Mean).getOrElse {
                throw IllegalStateException("no benchmark change statistic for $b")
            },
        )

    override fun getLength(): Int = nrBenchmarks

    companion object {
        private const val problemName = "Prioritization"
        private const val nrObjectives = 3

        private val allBenchs = Benchmark(
            clazz = "ALL",
            name = "BENCHS",
            params = listOf(),
            jmhParams = listOf(),
            returnType = "",
        )

        private data class Objectives(
            val bench: Benchmark,
            var coverage: Double = 0.0, // maximize objective -> initialize to minimum (0)
            var overlappingPercentage: Double = 1.0, // minimize objective -> initialize to maximum (1.0)
            var averageHistoricalPerformanceChange: Double = 0.0 // maximize objective -> initialize to minimum (0.0)
        )
    }
}
