package ch.uzh.ifi.seal.bencher.prioritization

import ch.uzh.ifi.seal.bencher.Method


data class PrioritizedMethod<T : Method>(
        val method: T,
        val priority: Priority
)

data class Priority(
        val rank: Int,
        val total: Int,
        val value: PriorityValue
)

sealed interface PriorityValue
data class PrioritySingle(
        val value: Double
) : PriorityValue
data class PriorityMultiple(
        val values: List<Double>
) : PriorityValue
