package ch.uzh.ifi.seal.bencher.analysis.finder.shared

class BenchModeAnnotation {
    private val arrayMode = mutableListOf<String>()

    fun mode(): List<String> = arrayMode

    fun setValueEnum(name: String?, descriptor: String, value: String) {
        if (descriptor == enum) {
            arrayMode.add(value)
        }
    }

    companion object {
        const val enum = "Lorg/openjdk/jmh/annotations/Mode;"
    }
}