package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.problem.permutationproblem.impl.AbstractIntegerPermutationProblem
import org.uma.jmetal.solution.permutationsolution.PermutationSolution


class PrioritizationProblem(
    private val benchmarkIdMap: BenchmarkIdMap,
    private val objectives: List<Objective>,
    private val aggregation: Aggregation? = null,
) : AbstractIntegerPermutationProblem() {

    override fun numberOfVariables(): Int = benchmarkIdMap.size

    override fun numberOfObjectives(): Int = if (aggregation == null) {
        objectives.size
    } else {
        1
    }

    override fun numberOfConstraints(): Int = 0

    override fun name(): String = "Prioritization"

    override fun evaluate(solution: PermutationSolution<Int>): PermutationSolution<Int> {
        val os = objectives.map { o ->
            Objective.toMinimization(
                o.type,
                o.compute(solution.variables(), benchmarkIdMap),
            )
        }

        if (aggregation == null) {
            os.forEachIndexed { i, ov ->
                solution.objectives()[i] = ov
            }
        } else {
            solution.objectives()[0] = aggregation.compute(os.toDoubleArray())
        }

        return solution
    }

    fun toSingleObjective(aggregate: Aggregation): PrioritizationProblem {
        if (this.aggregation != null) {
            throw IllegalStateException("PrioritizationProblem already a single-objective problem")
        }

        return PrioritizationProblem(benchmarkIdMap, objectives, aggregate)
    }

    fun toMultiObjective(): PrioritizationProblem = PrioritizationProblem(benchmarkIdMap, objectives)
}
