package ch.uzh.ifi.seal.bencher.analysis.finder

import org.objectweb.asm.AnnotationVisitor

class AsmBenchModeAnnotationVisitor(api: Int, av: AnnotationVisitor?) : AnnotationVisitor(api, av) {
    val arrayMode = mutableListOf<String>()

    fun mode(): List<String> = arrayMode

    override fun visitArray(name: String): AnnotationVisitor? {
        av?.visitArray(name)
        return this
    }

    override fun visit(name: String?, value: Any) {
        av?.visit(name, value)
        when (value) {
            is String -> arrayMode.add(value.toString())
            else -> return
        }
    }
}