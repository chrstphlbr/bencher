package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchIterationAnnotation
import org.funktionale.option.Option
import org.objectweb.asm.AnnotationVisitor
import java.util.concurrent.TimeUnit

class AsmBenchIterationAnnotationVisitor(api: Int, av: AnnotationVisitor?) : AnnotationVisitor(api, av) {

    val benchIterationAnnotation = BenchIterationAnnotation()

    // TODO remove
    fun iterations(): Int = benchIterationAnnotation.iterations()

    fun time(): Int = benchIterationAnnotation.time()
    fun timeUnit(): Option<TimeUnit> = benchIterationAnnotation.timeUnit()

    override fun visit(name: String?, value: Any) {
        av?.visit(name, value)
        benchIterationAnnotation.setValue(name, value)
    }

    override fun visitEnum(name: String?, descriptor: String, value: String) {
        av?.visitEnum(name, descriptor, value)
        benchIterationAnnotation.setValueEnum(name, descriptor, value)
    }
}
