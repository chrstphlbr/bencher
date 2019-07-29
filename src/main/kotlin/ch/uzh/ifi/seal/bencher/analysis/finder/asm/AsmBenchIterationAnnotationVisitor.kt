package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import org.funktionale.option.Option
import org.objectweb.asm.AnnotationVisitor
import java.util.concurrent.TimeUnit

class AsmBenchIterationAnnotationVisitor(api: Int, av: AnnotationVisitor?) : AnnotationVisitor(api, av) {

    private var iterations = defaultInt
    private var time = defaultInt
    private var timeUnit = defaultTimeUnit

    fun iterations(): Int = iterations
    fun time(): Int = time
    fun timeUnit(): Option<TimeUnit> = timeUnit

    override fun visit(name: String?, value: Any) {
        av?.visit(name, value)

        if (name == null) {
            // no default (value) value for iteration (measurement and warmup) annotations
            return
        }

        when (name) {
            valIteration -> iterations = intOrDefault(value)
            valTime -> time = intOrDefault(value)
        }
    }


    override fun visitEnum(name: String?, descriptor: String, value: String) {
        av?.visitEnum(name, descriptor, value)

        if (name == null) {
            // no default (value) value for iteration (measurement and warmup) annotations
            return
        }

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

    private fun intOrDefault(value: Any): Int =
            if (value is Int) {
                value
            } else {
                defaultInt
            }

    private fun timeUnitOrDefault(value: Any): Option<TimeUnit> =
            if (value is TimeUnit) {
                Option.Some(value)
            } else {
                defaultTimeUnit
            }

    companion object {
        private val defaultInt = -1
        private val defaultTimeUnit = Option.empty<TimeUnit>()

        private val valIteration = "iterations"
        private val valTime = "time"
        private val valTimeUnit = "timeUnit"

        private val bcTimeUnit = "Ljava/util/concurrent/TimeUnit;"
    }
}
