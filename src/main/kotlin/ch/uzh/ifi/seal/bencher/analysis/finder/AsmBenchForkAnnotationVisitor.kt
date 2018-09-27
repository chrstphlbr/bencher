package ch.uzh.ifi.seal.bencher.analysis.finder

import org.objectweb.asm.AnnotationVisitor

class AsmBenchForkAnnotationVisitor(api: Int, av: AnnotationVisitor?) : AnnotationVisitor(api, av) {

    private var forks: Int = defaultValue
    private var warmups: Int = defaultValue

    fun forks(): Int = forks
    fun warmups(): Int = warmups

    override fun visit(name: String?, value: Any) {
        av?.visit(name, value)

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
        private val defaultValue = -1

        private val valForks = "value"
        private val valWarmupForks = "warmups"
    }
}
