package ch.uzh.ifi.seal.bencher

fun <K, V> mapOfNotNull(pair: Pair<K, V>?): Map<K, V> {
    return pair?.let { mapOf(it) } ?: emptyMap()
}
