package ch.uzh.ifi.seal.bencher

private const val equal = 0

object MethodComparator : Comparator<Method> {
    private val c = compareBy(Method::clazz)
            .thenBy(Method::name)
            .thenComparing(ParameterComparator)
            .thenComparing(JmhParameterComparator)

    override fun compare(m1: Method, m2: Method): Int = c.compare(m1, m2)
}

private object ParameterComparator : Comparator<Method> {
    override fun compare(m1: Method, m2: Method): Int {
        val sizes = m1.params.size.compareTo(m2.params.size)
        if (sizes != equal) {
            return sizes
        }

        return m1.params.asSequence()
                .zip(m2.params.asSequence())
                .map { comparePair(it) }
                .find { it != equal }
                ?: equal
    }
}

private object JmhParameterComparator : Comparator<Method> {
    override fun compare(m1: Method, m2: Method): Int =
            if (m1 is Benchmark && m2 is Benchmark) {
                compare(m1, m2)
            } else {
                0
            }

    private fun compare(m1: Benchmark, m2: Benchmark): Int {
        val cs = m1.jmhParams.size.compareTo(m2.jmhParams.size)
        if (cs != equal) {
            return cs
        }

        val diff = m1.jmhParams.asSequence()
                .zip(m2.jmhParams.asSequence())
                .map { (m1p, m2p) ->
                    Pair(
                            m1p.first.compareTo(m2p.first),
                            m1p.second.compareTo(m2p.second)
                    )
                }
                .find { it.first != equal || it.second != equal }
                ?: return equal

        return when {
            diff.first != equal -> diff.first
            diff.second != equal -> diff.second
            else -> equal
        }
    }
}

object LineComparator : Comparator<Line> {
    val c = compareBy(Line::file).thenBy(Line::number)

    override fun compare(l1: Line, l2: Line): Int = c.compare(l1, l2)
}

private fun <T : Comparable<T>> comparePair(p: Pair<T, T>): Int = p.first.compareTo(p.second)
