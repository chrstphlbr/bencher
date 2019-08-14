package ch.uzh.ifi.seal.bencher.analysis.finder.shared

class BenchGroupAnnotation {

    private var name = defaultValue
    fun name() = name

    fun setValue(name: String?, value: Any) {
        this.name = valueOrDefault(value)
    }

    private fun valueOrDefault(value: Any): String? =
            if (value is String && value.isNotEmpty()) {
                value
            } else {
                defaultValue
            }

    companion object {
        private val defaultValue: String? = null
    }
}