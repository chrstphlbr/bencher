package ch.uzh.ifi.seal.bencher.prioritization.search

interface ObjectiveFunction {
    val min: Double
    val max: Double
    fun compute(l: List<Double>): Double
}

// similar to APFD-P from Mostafa et al. "PerfRanker: Prioritization of Performance Regression Tests for Collection-Intensive Software" [ISSTA '17],
// but instead of change, we use the Double value here
class AveragePercentage(
    private val defaultEmptyList: Double = -1.0,
    private val defaultListSumZero: Double = -2.0,
) : ObjectiveFunction {
    override val min: Double = 0.0
    override val max: Double = 1.0

    override fun compute(l: List<Double>): Double {
        val n = l.size.toDouble()
        if (n == 0.0) {
            return defaultEmptyList
        }

        val t = l.fold(0.0, Double::plus)
        if (t == 0.0) {
            return defaultListSumZero
        }

        val mem = mutableListOf<Double>()
        mem.add(0.0) // have the zero value for sums at position 0

        val sum = l.foldIndexed(0.0) { i, acc, change ->
            val prevDetected = mem[i]
            val newDetected = prevDetected + change
            mem.add(newDetected)
            acc + newDetected / t
        }

        return sum / n
    }
}
