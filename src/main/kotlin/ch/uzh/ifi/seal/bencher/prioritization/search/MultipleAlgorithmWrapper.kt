package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.algorithm.Algorithm
import org.uma.jmetal.solution.Solution

class MultipleAlgorithmWrapper<S>(
    private val algorithms: List<Algorithm<List<S>>>,
) : Algorithm<List<S>> where S : Solution<*> {
    private val solutions = mutableListOf<S>()
    private val algorithmNames = algorithms.joinToString(", ") { it.name() }
    private val algorithmDescriptions = algorithms.joinToString(", ") { it.description() }

    override fun run() {
        algorithms.forEach { a ->
            a.run()
            val s = a.result()
            solutions.addAll(s)
        }
    }

    override fun result(): List<S> = solutions

    override fun name(): String = "$algorithmNames (multiple solutions wrapper)"

    override fun description(): String = "$algorithmDescriptions (multiple solutions wrapper)"
}
