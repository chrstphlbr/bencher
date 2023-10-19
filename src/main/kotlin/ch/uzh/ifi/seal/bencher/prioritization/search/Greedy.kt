package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.algorithm.Algorithm
import org.uma.jmetal.problem.permutationproblem.PermutationProblem
import org.uma.jmetal.solution.permutationsolution.PermutationSolution
import org.uma.jmetal.solution.permutationsolution.impl.IntegerPermutationSolution

class Greedy(
    private val problem: PermutationProblem<PermutationSolution<Int>>,
    private val benchmarkIdMap: BenchmarkIdMap,
    private val objectives: List<Objective>,
    private val aggregation: Aggregation?,
) : Algorithm<PermutationSolution<Int>> {

    private lateinit var result: PermutationSolution<Int>

    init {
        if (problem.numberOfVariables() != benchmarkIdMap.size) {
            throw IllegalArgumentException("problem.numberOfVariables (${problem.numberOfVariables()}) != benchmarkIdMap.size (${benchmarkIdMap.size})")
        }

        if (objectives.isEmpty()) {
            throw IllegalArgumentException("no objectives specified")
        }

        if (objectives.size == 1 && aggregation != null) {
            throw IllegalArgumentException("aggregation not null although objectives.size == 1")
        }

        if (objectives.size > 1 && aggregation == null) {
            throw IllegalArgumentException("objectives.size > 1 but aggregation == null")
        }
    }

    override fun run() {
        val greedySolution = greedySolution()
        result = problem.evaluate(greedySolution)
    }

    private fun greedySolution(): PermutationSolution<Int> {
        val idsSingleObjectives = (0 until benchmarkIdMap.size)
            .map { id ->
                val b = benchmarkIdMap[id] ?: throw IllegalStateException("expected benchmark with id $id")
                val objectiveValues = objectives.map { o -> Objective.toMinimization(o.type, o.compute(b)) }
                val objectiveValue = aggregation?.compute(objectiveValues.toDoubleArray()) ?: objectiveValues[0]
                Pair(id, objectiveValue)
            }
            .sortedBy { it.second }

        assert(idsSingleObjectives.size == benchmarkIdMap.size)

        val solution = IntegerPermutationSolution(benchmarkIdMap.size, problem.numberOfObjectives(), problem.numberOfConstraints())

        assert(idsSingleObjectives.size == solution.variables().size)

        idsSingleObjectives.forEachIndexed { i, (v, _) -> solution.variables()[i] = v }

        return solution
    }

    override fun result(): PermutationSolution<Int> = result

    override fun name(): String = "Greedy"

    override fun description(): String = "Greedy"
}
