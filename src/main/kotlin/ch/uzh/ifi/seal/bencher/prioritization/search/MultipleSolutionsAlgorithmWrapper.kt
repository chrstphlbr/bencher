package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.algorithm.Algorithm
import org.uma.jmetal.solution.Solution

class MultipleSolutionsAlgorithmWrapper<S>(
    private val algorithm: Algorithm<S>,
) : Algorithm<List<S>> where S : Solution<*> {
    private lateinit var solution: S

    override fun run() {
        algorithm.run()
        solution = algorithm.result()
    }

    override fun result(): List<S> = listOf(solution)

    override fun name(): String = "${algorithm.name()} (multiple solutions wrapper)"

    override fun description(): String = "${algorithm.description()} (multiple solutions wrapper)"
}
