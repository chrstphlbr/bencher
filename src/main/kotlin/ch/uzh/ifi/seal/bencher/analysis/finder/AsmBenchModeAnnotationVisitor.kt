package ch.uzh.ifi.seal.bencher.analysis.finder

import org.objectweb.asm.AnnotationVisitor

class AsmBenchModeAnnotationVisitor(api: Int, av: AnnotationVisitor?) : AnnotationVisitor(api, av) {
    val arrayMode = mutableListOf<String>()

    fun mode(): List<String> = arrayMode

    override fun visitArray(name: String): AnnotationVisitor? {
        av?.visitArray(name)
        return this
    }

    override fun visitEnum(name: String?, descriptor: String, value: String) {
        av?.visitEnum(name, descriptor, value)

        if (descriptor == emum) {
            arrayMode.add(value)
        }
    }

    companion object {
        private val emum = "Lorg/openjdk/jmh/annotations/Mode;"
    }
}