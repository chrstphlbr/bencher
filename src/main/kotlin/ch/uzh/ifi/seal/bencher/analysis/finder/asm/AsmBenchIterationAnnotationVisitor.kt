package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchIterationAnnotation
import org.objectweb.asm.AnnotationVisitor

class AsmBenchIterationAnnotationVisitor(api: Int, av: AnnotationVisitor?) : AnnotationVisitor(api, av) {

    val benchIterationAnnotation = BenchIterationAnnotation()

    override fun visit(name: String?, value: Any) {
        av?.visit(name, value)
        benchIterationAnnotation.setValue(name, value)
    }

    override fun visitEnum(name: String?, descriptor: String, value: String) {
        av?.visitEnum(name, descriptor, value)
        benchIterationAnnotation.setValueEnum(name, descriptor, value)
    }
}
