package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.solution.permutationsolution.PermutationSolution
import org.uma.jmetal.util.neighborhood.Neighborhood

// Implementation of a test suite neighborhood as defined in Li et al. "Search Algorithms for Regression Test Case Prioritization"
// The neighborhood is defined by all orderings where the first variable is swapped with each other variable
class PermutationNeighborhood<T> : Neighborhood<PermutationSolution<T>> {
    // do not pick neighbors within the solutionList, but compute the neighbors for the solution at the index
    override fun getNeighbors(
        solutionList: List<PermutationSolution<T>>,
        solutionIndex: Int,
    ): List<PermutationSolution<T>> {
        if (solutionList.isEmpty()) {
            throw IllegalArgumentException("solutionList is empty")
        }

        if (solutionIndex < 0 || solutionIndex >= solutionList.size) {
            throw IllegalArgumentException("illegal solutionIndex $solutionIndex (solutionList.size == ${solutionList.size})")
        }

        val s = solutionList[solutionIndex]

        return getNeighbors(s)
    }

    private fun getNeighbors(s: PermutationSolution<T>): List<PermutationSolution<T>> {
        val l = s.variables().size
        if (l == 1) {
            return listOf()
        }

        val first = s.variables()[0]

        return (1 until l).map { i ->
            val neighbor = s.copy() as PermutationSolution<T>
            neighbor.variables()[0] = neighbor.variables()[i]
            neighbor.variables()[i] = first
            neighbor
        }
    }
}
