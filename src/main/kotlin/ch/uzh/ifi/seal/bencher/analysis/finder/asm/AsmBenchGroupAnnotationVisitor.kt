package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchGroupAnnotation
import org.objectweb.asm.AnnotationVisitor

class AsmBenchGroupAnnotationVisitor(api: Int, av: AnnotationVisitor?) : AnnotationVisitor(api, av) {

    val benchGroupAnnotation = BenchGroupAnnotation()

    override fun visit(name: String?, value: Any) {
        av?.visit(name, value)
        benchGroupAnnotation.setValue(name, value)
    }
}