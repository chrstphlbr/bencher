package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchModeAnnotation
import org.objectweb.asm.AnnotationVisitor

class AsmBenchModeAnnotationVisitor(api: Int, av: AnnotationVisitor?) : AnnotationVisitor(api, av) {
    val benchModeAnnotation = BenchModeAnnotation()

    // TODO remove
    fun mode(): List<String> = benchModeAnnotation.mode()

    override fun visitArray(name: String): AnnotationVisitor? {
        av?.visitArray(name)
        return this
    }

    override fun visitEnum(name: String?, descriptor: String, value: String) {
        av?.visitEnum(name, descriptor, value)
        benchModeAnnotation.setValueEnum(name, descriptor, value)
    }
}