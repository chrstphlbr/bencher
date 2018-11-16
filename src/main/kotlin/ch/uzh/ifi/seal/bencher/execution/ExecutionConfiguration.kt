package ch.uzh.ifi.seal.bencher.execution

import ch.uzh.ifi.seal.bencher.JMHVersion
import org.funktionale.option.Option
import java.util.concurrent.TimeUnit

data class ExecutionConfiguration(
        val warmupIterations: Int,
        val warmupTime: Int,
        val warmupTimeUnit: Option<TimeUnit>,
        val measurementIterations: Int,
        val measurementTime: Int,
        val measurementTimeUnit: Option<TimeUnit>,
        val forks: Int,
        val warmupForks: Int,
        val mode: List<String>,
        val outputTimeUnit: Option<TimeUnit>
) {
    infix fun orDefault(default: ExecutionConfiguration): ExecutionConfiguration =
            ExecutionConfiguration(
                    warmupIterations = if (this.warmupIterations != unsetExecConfig.warmupIterations) {
                        this.warmupIterations
                    } else {
                        default.warmupIterations
                    },
                    warmupTime = if (this.warmupTime != unsetExecConfig.warmupTime) {
                        this.warmupTime
                    } else {
                        default.warmupTime
                    },
                    warmupTimeUnit = if (this.warmupTimeUnit.isDefined()) {
                        this.warmupTimeUnit
                    } else {
                        default.warmupTimeUnit
                    },
                    measurementIterations = if (this.measurementIterations != unsetExecConfig.measurementIterations) {
                        this.measurementIterations
                    } else {
                        default.measurementIterations
                    },
                    measurementTime = if (this.measurementTime != unsetExecConfig.measurementTime) {
                        this.measurementTime
                    } else {
                        default.measurementTime
                    },
                    measurementTimeUnit = if (this.measurementTimeUnit.isDefined()) {
                        this.measurementTimeUnit
                    } else {
                        default.measurementTimeUnit
                    },
                    forks = if (this.forks != unsetExecConfig.forks) {
                        this.forks
                    } else {
                        default.forks
                    },
                    warmupForks = if (this.warmupForks != unsetExecConfig.warmupForks) {
                        this.warmupForks
                    } else {
                        default.warmupForks
                    },
                    mode = if (this.mode != unsetExecConfig.mode) {
                        this.mode
                    } else {
                        default.mode
                    },
                    outputTimeUnit = if (this.outputTimeUnit != unsetExecConfig.outputTimeUnit) {
                        this.outputTimeUnit
                    } else {
                        default.outputTimeUnit
                    }
            )
}

val unsetExecConfig = ExecutionConfiguration(
        warmupIterations = -1,
        warmupTime = -1,
        warmupTimeUnit = Option.empty(),
        measurementIterations = -1,
        measurementTime = -1,
        measurementTimeUnit = Option.empty(),
        forks = -1,
        warmupForks = -1,
        mode = listOf(),
        outputTimeUnit = Option.empty()
)

fun defaultExecConfig(version: JMHVersion): ExecutionConfiguration {
    val jmh120 = JMHVersion(major = 1, minor = 20)
    return if (version.compareTo(jmh120) == 1) {
        DefaultExecConfig.post121
    } else {
        DefaultExecConfig.pre121
    }
}

private object DefaultExecConfig {
    internal val post121 = ExecutionConfiguration(
            warmupIterations = 5,
            warmupTime = 10,
            warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
            measurementIterations = 5,
            measurementTime = 10,
            measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
            forks = 5,
            warmupForks = 0,
            mode = listOf(),
            outputTimeUnit = Option.Some(TimeUnit.SECONDS)
    )

    internal val pre121 = ExecutionConfiguration(
            warmupIterations = 20,
            warmupTime = 1,
            warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
            measurementIterations = 20,
            measurementTime = 1,
            measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
            forks = 10,
            warmupForks = 0,
            mode = listOf(),
            outputTimeUnit = Option.Some(TimeUnit.SECONDS)
    )
}