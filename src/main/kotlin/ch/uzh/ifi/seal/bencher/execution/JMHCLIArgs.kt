package ch.uzh.ifi.seal.bencher.execution

import com.beust.jcommander.*
import org.funktionale.option.Option
import java.sql.Time
import java.util.concurrent.TimeUnit

class JMHCLIArgs {
    @Parameter(names = ["-wi"], description = "warmup iterations")
    var warmupIterations: Int = unsetExecConfig.warmupIterations
    @Parameter(names = ["-w"], description = "warmup-iteration time")
    var warmupTime: Int = unsetExecConfig.warmupTime
    // not a JMH CLI parameter
    var warmupTimeUnit: TimeUnit? = null
    @Parameter(names = ["-i"], description = "measurement iterations")
    var measurementIterations: Int = unsetExecConfig.measurementIterations
    @Parameter(names = ["-r"], description = "measurement-iteration time")
    var measurementTime: Int = unsetExecConfig.measurementTime
    // not a JMH CLI parameter
    var measurementTimeUnit: TimeUnit? = null
    @Parameter(names = ["-f"], description = "number of forks")
    var forks: Int = unsetExecConfig.forks
    @Parameter(names = ["-wf"], description = "number of warmup forks")
    var warmupForks: Int = unsetExecConfig.warmupForks
    @Parameter(
            names = ["-bm"],
            description = "benchmark mode",
            validateWith = [BenchmarkModeValidator::class]
    )
    var mode: List<String> = unsetExecConfig.mode
    @Parameter(
            names = ["-tu"],
            description = "output time unit",
            validateWith = [OutputTimeUnitValidator::class],
            converter = OutputTimeUnitConverter::class
    )
    var outputTimeUnit: TimeUnit? = null

    fun execConfig(): ExecutionConfiguration =
            ExecutionConfiguration(
                    warmupIterations = this.warmupIterations,
                    warmupTime = this.warmupTime,
                    warmupTimeUnit = if (this.warmupTimeUnit != null) {
                        Option.Some(this.warmupTimeUnit!!)
                    } else {
                        Option.empty()
                    },
                    measurementIterations = this.measurementIterations,
                    measurementTime = this.measurementTime,
                    measurementTimeUnit = if (this.measurementTimeUnit != null) {
                        Option.Some(this.measurementTimeUnit!!)
                    } else {
                        Option.empty()
                    },
                    forks = this.forks,
                    warmupForks = this.warmupForks,
                    mode = this.mode,
                    outputTimeUnit = if (this.outputTimeUnit != null) {
                        Option.Some(this.outputTimeUnit!!)
                    } else {
                        Option.empty()
                    }
            )
}

class BenchmarkModeValidator : IParameterValidator {
    override fun validate(name: String?, value: String?) {
        if (value == null) {
            throw ParameterException("Value for $name is null")
        }


        if (!values.contains(value)) {
            throw ParameterException("Value for $name is invalid: $value")
        }
    }

    companion object {
        val values = listOf("Throughput", "thrpt", "AverageTime", "avgt", "SampleTime", "sample", "SingleShotTime", "ss", "All", "all")
    }
}


private const val minutes = "m"
private const val seconds = "s"
private const val milliseconds = "ms"
private const val microseconds = "us"
private const val nanoseconds = "ns"

class OutputTimeUnitValidator : IParameterValidator {
    override fun validate(name: String?, value: String?) {
        if (value == null) {
            throw ParameterException("Value for $name is null")
        }


        if (!values.contains(value)) {
            throw ParameterException("Value for $name is invalid: $value")
        }
    }

    companion object {
        val values = listOf(minutes, seconds, milliseconds, microseconds, nanoseconds)
    }
}

class OutputTimeUnitConverter : IStringConverter<TimeUnit> {
    override fun convert(value: String?): TimeUnit {
        if (value == null) {
            return TimeUnit.SECONDS
        }

        return when (value) {
            minutes -> TimeUnit.MINUTES
            seconds -> TimeUnit.SECONDS
            milliseconds -> TimeUnit.MILLISECONDS
            microseconds -> TimeUnit.MICROSECONDS
            nanoseconds -> TimeUnit.NANOSECONDS
            else -> TimeUnit.SECONDS
        }
    }
}

fun parseJMHCLIParameter(s: String): JMHCLIArgs {
    val splitted = s.split(" ")
    val args = JMHCLIArgs()
    val jc = JCommander.newBuilder()
            .programName("JMH CLI")
            .acceptUnknownOptions(true)
            .addObject(args)
            .build()

    val array = splitted.toTypedArray()
    jc.parse(*array)
    return args
}
