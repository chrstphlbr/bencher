package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchForkAnnotation
import org.objectweb.asm.AnnotationVisitor

class AsmBenchForkAnnotationVisitor(api: Int, av: AnnotationVisitor?) : AnnotationVisitor(api, av) {

    val benchForkAnnotation = BenchForkAnnotation()

    // TODO remove
    fun forks(): Int = benchForkAnnotation.forks()

    fun warmups(): Int = benchForkAnnotation.warmups()

    override fun visit(name: String?, value: Any) {
        av?.visit(name, value)
        benchForkAnnotation.setValue(name, value)
    }
}
