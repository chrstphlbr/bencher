package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.algorithm.impl.AbstractLocalSearch
import org.uma.jmetal.problem.Problem
import org.uma.jmetal.solution.Solution
import org.uma.jmetal.util.neighborhood.Neighborhood

// HillClimbing is an implementation of the steepest ascent version hill climbing algorithm
class HillClimbing<S>(
    private val initialSolution: S, // expects an evaluated solution
    private val problem: Problem<S>,
    private val comparator: Comparator<S>,
    private val neighborhood: Neighborhood<S>,
    private val maxEvaluations: Int,
) : AbstractLocalSearch<S>() where S : Solution<*> {

    private var currentChanged: Boolean = true
    private val evaluatedSolutions: MutableMap<S, S> = mutableMapOf()
    private var evaluations: Int = 0

    override fun name(): String = "Hill Climbing"

    override fun description(): String = "Hill Climbing"

    override fun setCurrentSolution(): S = initialSolution

    override fun initProgress() {
        evaluations = 1
    }

    override fun updateProgress() {
        evaluations++
    }

    override fun isStoppingConditionReached(): Boolean {
        return !currentChanged || evaluations > maxEvaluations
    }

    override fun updateCurrentSolution(currentSolution: S): S {
        // this assumes an implementation like PermutationNeighborhood, where the neighborhood is not calculated
        // from the solutionList but on the spot for a single solution from the solutionList
        val ns = neighborhood.getNeighbors(listOf(currentSolution), 0)
        if (ns.isEmpty()) {
            currentChanged = false
            return currentSolution
        }

        val betterNeighbors = ns.asSequence()
            .map { memorizedEvaluate(it) }
            .sortedWith(comparator)
            .takeWhile { comparator.compare(it, currentSolution) < 0 }
            .toList()

        if (betterNeighbors.isEmpty()) {
            currentChanged = false
            return currentSolution
        }

        return betterNeighbors[0]
    }

    private fun memorizedEvaluate(solution: S): S = evaluatedSolutions.getOrPut(solution) { problem.evaluate(solution) }
}
