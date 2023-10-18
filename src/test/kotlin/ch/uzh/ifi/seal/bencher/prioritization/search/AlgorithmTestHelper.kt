package ch.uzh.ifi.seal.bencher.prioritization.search

import org.uma.jmetal.algorithm.Algorithm
import org.uma.jmetal.solution.integersolution.impl.DefaultIntegerSolution
import org.uma.jmetal.util.bounds.Bounds

object AlgorithmTestHelper {
    private fun bounds(size: Int): Bounds<Int> {
        return object : Bounds<Int> {
            override fun getLowerBound(): Int = 0
            override fun getUpperBound(): Int = size
        }
    }

    class AlgorithmSingleResultMock(private val variableValue: Int) : Algorithm<DefaultIntegerSolution> {
        private lateinit var result: DefaultIntegerSolution

        override fun run() {
            val s = DefaultIntegerSolution(listOf(bounds(1)), 0, 0)
            s.variables()[0] = variableValue
            result = s
        }

        override fun result(): DefaultIntegerSolution = result

        override fun name(): String = "AlgorithmSingleResultMock"

        override fun description(): String = "AlgorithmSingleResultMock description"
    }

    class AlgorithmMultipleResultsMock(private val size: Int) : Algorithm<List<DefaultIntegerSolution>> {
        private lateinit var results: List<DefaultIntegerSolution>

        override fun run() {
            val bounds = listOf(bounds(size))
            val rs = (1 .. size).map { i ->
                val s = DefaultIntegerSolution(bounds, 0, 0)
                s.variables()[0] = i
                s
            }
            assert(rs.size == size)
            results = rs
        }

        override fun result(): List<DefaultIntegerSolution> = results

        override fun name(): String = "AlgorithmMultipleResultsMock"

        override fun description(): String = "AlgorithmMultipleResultsMock description"
    }
}
