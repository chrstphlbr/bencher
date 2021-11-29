package ch.uzh.ifi.seal.bencher.prioritization

import ch.uzh.ifi.seal.bencher.Method


data class Priority(
        val rank: Int,
        val total: Int,
        val value: Double
)

data class PrioritizedMethod<T : Method>(
        val method: T,
        val priority: Priority
)
