package ch.uzh.ifi.seal.bencher.analysis.finder

import org.funktionale.option.Option
import org.objectweb.asm.AnnotationVisitor
import java.util.concurrent.TimeUnit

class AsmBenchOutputTimeUnitAnnotationVisitor(api: Int, av: AnnotationVisitor?) : AnnotationVisitor(api, av) {

    private var timeUnit = defaultTimeUnit
    fun timeUnit(): Option<TimeUnit> = timeUnit

    override fun visitEnum(name: String?, descriptor: String, value: String) {
        av?.visitEnum(name, descriptor, value)

        timeUnit = if (name == valTimeUnit && descriptor == bcTimeUnit) {
            try {
                Option.Some(TimeUnit.valueOf(value))
            } catch (e: IllegalArgumentException) {
                defaultTimeUnit
            }
        } else {
            defaultTimeUnit
        }
    }

    companion object {
        private val defaultTimeUnit = Option.empty<TimeUnit>()

        private val valTimeUnit = "value"

        private val bcTimeUnit = "Ljava/util/concurrent/TimeUnit;"
    }
}