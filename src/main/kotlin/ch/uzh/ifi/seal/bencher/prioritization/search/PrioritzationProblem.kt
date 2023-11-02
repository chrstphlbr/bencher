package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.problem.permutationproblem.impl.AbstractIntegerPermutationProblem
import org.uma.jmetal.solution.permutationsolution.PermutationSolution
import org.uma.jmetal.util.observable.Observable
import org.uma.jmetal.util.observable.ObservableEntity
import org.uma.jmetal.util.observable.impl.DefaultObservable


class PrioritizationProblem(
    private val benchmarkIdMap: BenchmarkIdMap,
    private val objectives: List<Objective>,
    private val aggregation: Aggregation? = null,
) : AbstractIntegerPermutationProblem(), ObservableEntity<Map<String, Any>> {

    private var nrEvaluations: Int = 0

    private val observable: Observable<Map<String, Any>> = DefaultObservable("Prioritization Problem");

    override fun numberOfVariables(): Int = benchmarkIdMap.size

    override fun numberOfObjectives(): Int = if (aggregation == null) {
        objectives.size
    } else {
        1
    }

    override fun numberOfConstraints(): Int = 0

    override fun name(): String = "Prioritization"

    override fun evaluate(solution: PermutationSolution<Int>): PermutationSolution<Int> {
        nrEvaluations++
        if (nrEvaluations == 1) {
            benchmarkIdsNotification(solution)
        }

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

        val attributes: Map<String, Any> = mapOf(
            Pair(SOLUTION_KEY, solution)
        )

        observable.setChanged()
        observable.notifyObservers(attributes)

        return solution
    }

    private fun benchmarkIdsNotification(solution: PermutationSolution<Int>) {
        val benchmarkIds = solution.variables().sorted().map { id ->
            val benchmark = benchmarkIdMap[id] ?: throw IllegalStateException("no benchmark for id $id")
            Pair(id!!, benchmark)
        }

        val attributes: Map<String, Any> = mapOf(
            Pair(BENCHMARK_IDS_KEY, benchmarkIds)
        )

        observable.setChanged()
        observable.notifyObservers(attributes)
    }

    override fun observable(): Observable<Map<String, Any>> = observable

    companion object {
        const val SOLUTION_KEY = "SOLUTION"
        const val BENCHMARK_IDS_KEY = "BENCHMARK_IDS"
    }
}
