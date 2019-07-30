package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchOutputTimeUnitAnnotation
import org.funktionale.option.Option
import org.objectweb.asm.AnnotationVisitor
import java.util.concurrent.TimeUnit

class AsmBenchOutputTimeUnitAnnotationVisitor(api: Int, av: AnnotationVisitor?) : AnnotationVisitor(api, av) {

    val benchOutputTimeUnitAnnotation = BenchOutputTimeUnitAnnotation()

    // TODO remove
    fun timeUnit(): Option<TimeUnit> = benchOutputTimeUnitAnnotation.timeUnit()

    override fun visitEnum(name: String?, descriptor: String, value: String) {
        av?.visitEnum(name, descriptor, value)
        benchOutputTimeUnitAnnotation.setValueEnum(name, descriptor, value)
    }
}