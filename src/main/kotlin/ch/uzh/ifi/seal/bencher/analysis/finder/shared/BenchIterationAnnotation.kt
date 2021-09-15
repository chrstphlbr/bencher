package ch.uzh.ifi.seal.bencher.analysis.finder.shared

import arrow.core.Option
import arrow.core.Some
import arrow.core.none
import java.util.concurrent.TimeUnit

class BenchIterationAnnotation {

    private var iterations = defaultInt
    private var time = defaultInt
    private var timeUnit = defaultTimeUnit

    fun iterations(): Int = iterations
    fun time(): Int = time
    fun timeUnit(): Option<TimeUnit> = timeUnit

    fun setValue(name: String?, value: Any) {
        if (name == null) {
            // no default (value) value for iteration (measurement and warmup) annotations
            return
        }

        when (name) {
            valIteration -> iterations = intOrDefault(value)
            valTime -> time = intOrDefault(value)
        }
    }


    fun setValueEnum(name: String?, descriptor: String, value: String) {
        if (name == null) {
            // no default (value) value for iteration (measurement and warmup) annotations
            return
        }

        timeUnit = if (name == valTimeUnit && descriptor == bcTimeUnit) {
            try {
                Some(TimeUnit.valueOf(value))
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
                Some(value)
            } else {
                defaultTimeUnit
            }

    companion object {
        private const val defaultInt = -1
        private val defaultTimeUnit = none<TimeUnit>()

        private const val valIteration = "iterations"
        private const val valTime = "time"
        private const val valTimeUnit = "timeUnit"

        val bcTimeUnit = "Ljava/util/concurrent/TimeUnit;"
    }
}
