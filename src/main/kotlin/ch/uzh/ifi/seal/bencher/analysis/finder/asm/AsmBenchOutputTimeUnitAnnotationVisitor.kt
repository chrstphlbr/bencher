package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchOutputTimeUnitAnnotation
import org.objectweb.asm.AnnotationVisitor

class AsmBenchOutputTimeUnitAnnotationVisitor(api: Int, av: AnnotationVisitor?) : AnnotationVisitor(api, av) {

    val benchOutputTimeUnitAnnotation = BenchOutputTimeUnitAnnotation()

    override fun visitEnum(name: String?, descriptor: String, value: String) {
        av?.visitEnum(name, descriptor, value)
        benchOutputTimeUnitAnnotation.setValueEnum(name, descriptor, value)
    }
}