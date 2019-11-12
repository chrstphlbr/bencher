package ch.uzh.ifi.seal.bencher.analysis.finder.shared

class BenchForkAnnotation {

    private var forks: Int = defaultValue
    private var warmups: Int = defaultValue

    fun forks(): Int = forks
    fun warmups(): Int = warmups

    fun setValue(name: String?, value: Any) {
        if (name == null) {
            // forks
            forks = valueOrDefault(value)
        } else {
            when (name) {
                valForks -> forks = valueOrDefault(value)
                valWarmupForks -> warmups = valueOrDefault(value)
            }
        }
    }

    private fun valueOrDefault(value: Any): Int =
            if (value is Int) {
                value
            } else {
                defaultValue
            }

    companion object {
        private const val defaultValue = -1

        private const val valForks = "value"
        private const val valWarmupForks = "warmups"
    }
}
