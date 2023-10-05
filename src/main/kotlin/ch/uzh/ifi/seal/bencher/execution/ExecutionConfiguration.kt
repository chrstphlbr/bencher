package ch.uzh.ifi.seal.bencher.execution

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import ch.uzh.ifi.seal.bencher.JMHVersion
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
                    warmupTimeUnit = if (this.warmupTimeUnit.isSome()) {
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
                    measurementTimeUnit = if (this.measurementTimeUnit.isSome()) {
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
        warmupTimeUnit = None,
        measurementIterations = -1,
        measurementTime = -1,
        measurementTimeUnit = None,
        forks = -1,
        warmupForks = -1,
        mode = listOf(),
        outputTimeUnit = None
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
    val post121 = ExecutionConfiguration(
            warmupIterations = 5,
            warmupTime = 10,
            warmupTimeUnit = Some(TimeUnit.SECONDS),
            measurementIterations = 5,
            measurementTime = 10,
            measurementTimeUnit = Some(TimeUnit.SECONDS),
            forks = 5,
            warmupForks = 0,
            mode = listOf("Throughput"),
            outputTimeUnit = Some(TimeUnit.SECONDS)
    )

    val pre121 = ExecutionConfiguration(
            warmupIterations = 20,
            warmupTime = 1,
            warmupTimeUnit = Some(TimeUnit.SECONDS),
            measurementIterations = 20,
            measurementTime = 1,
            measurementTimeUnit = Some(TimeUnit.SECONDS),
            forks = 10,
            warmupForks = 0,
            mode = listOf("Throughput"),
            outputTimeUnit = Some(TimeUnit.SECONDS)
    )
}