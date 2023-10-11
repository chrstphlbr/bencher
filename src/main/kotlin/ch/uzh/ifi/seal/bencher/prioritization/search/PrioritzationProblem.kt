package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.problem.permutationproblem.impl.AbstractIntegerPermutationProblem
import org.uma.jmetal.solution.permutationsolution.PermutationSolution


class PrioritizationProblem(
    private val benchmarkIdMap: BenchmarkIdMap,
    private val objectives: List<Objective>,
    private val aggregate: Aggregation? = null,
) : AbstractIntegerPermutationProblem() {

    override fun numberOfVariables(): Int = benchmarkIdMap.size

    override fun numberOfObjectives(): Int = if (aggregate == null) {
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

        if (aggregate == null) {
            os.forEachIndexed { i, ov ->
                solution.objectives()[i] = ov
            }
        } else {
            solution.objectives()[0] = aggregate.compute(os.toDoubleArray())
        }

        return solution
    }
}
