package ch.uzh.ifi.seal.bencher.prioritization.search

// similar to APFD-P from Mostafa et al. -- "PerfRanker: Prioritization of Performance Regression Tests for Collection-Intensive Software" [ISSTA '17],
// but instead of change, we use the Double value here
fun averagePercentage(l: List<Double>, defaultEmptyList: Double = -1.0, defaultListSumZero: Double = -2.0): Double {
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

sealed interface Objective {
    val maximize: Boolean

    val startValue: Double
        get() = defaultStartValue(maximize)

    fun toMinimization(value: Double): Double = if (maximize) {
        value * -1
    } else {
        value
    }

    companion object {
        fun defaultStartValue(maximize: Boolean) = if (maximize) {
            0.0 // maximize objective -> initialize to minimum (0.0)
        } else {
            1.0 // minimize objective -> initialize to maximum (1.0)
        }
    }
}

object CoverageObjective : Objective {
    override val maximize: Boolean
        get() = true
}

object DeltaCoverageObjective : Objective {
    override val maximize: Boolean
        get() = true
}

object CoverageOverlapObjective : Objective {
    override val maximize: Boolean
        get() = false
}

object ChangeHistoryObjective : Objective {
    override val maximize: Boolean
        get() = true
}
