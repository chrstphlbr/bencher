package ch.uzh.ifi.seal.bencher.execution

import org.funktionale.option.Option
import picocli.CommandLine
import java.util.concurrent.TimeUnit

@CommandLine.Command(name = "jmh")
class JMHCLIArgs {
    @CommandLine.Spec
    private lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.Option(names = ["-wi"], description = ["warmup iterations"])
    var warmupIterations: Int = unsetExecConfig.warmupIterations
    @CommandLine.Option(names = ["-w"], description = ["warmup-iteration time"])
    var warmupTime: Int = unsetExecConfig.warmupTime
    // not a JMH CLI parameter
    var warmupTimeUnit: TimeUnit? = null
    @CommandLine.Option(names = ["-i"], description = ["measurement iterations"])
    var measurementIterations: Int = unsetExecConfig.measurementIterations
    @CommandLine.Option(names = ["-r"], description = ["measurement-iteration time"])
    var measurementTime: Int = unsetExecConfig.measurementTime
    // not a JMH CLI parameter
    var measurementTimeUnit: TimeUnit? = null
    @CommandLine.Option(names = ["-f"], description = ["number of forks"])
    var forks: Int = unsetExecConfig.forks
    @CommandLine.Option(names = ["-wf"], description = ["number of warmup forks"])
    var warmupForks: Int = unsetExecConfig.warmupForks

    var mode: List<String> = unsetExecConfig.mode
        @CommandLine.Option(
                names = ["-bm"],
                description = ["benchmark mode"]
//                validateWith = [BenchmarkModeValidator::class]
        )
        set(value) {
            BenchmarkModeValidator.validate(spec, "mode", value)
            field = value
        }


    @CommandLine.Option(
            names = ["-tu"],
            description = ["output time unit"],
            converter = [OutputTimeUnitConverter::class]
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

object BenchmarkModeValidator {
    private val values = setOf("Throughput", "thrpt", "AverageTime", "avgt", "SampleTime", "sample", "SingleShotTime", "ss", "All", "all")

    fun validate(spec: CommandLine.Model.CommandSpec, name: String, value: List<String>) {
//        if (value == null) {
//            throw CommandLine.ParameterException(spec.commandLine(), "Value for $name is null")
//        }

        value.forEach {
            if (!values.contains(it)) {
                throw CommandLine.ParameterException(spec.commandLine(), "Value for $name is invalid: $it")
            }
        }
    }
}


private const val minutes = "m"
private const val seconds = "s"
private const val milliseconds = "ms"
private const val microseconds = "us"
private const val nanoseconds = "ns"

class OutputTimeUnitConverter : CommandLine.ITypeConverter<TimeUnit> {
    override fun convert(value: String?): TimeUnit {
        if (value == null) {
            throw IllegalArgumentException("OutputTimeUnit is null")
        }


        if (!values.contains(value)) {
            throw IllegalArgumentException("OutputTimeUnit is invalid: $value")
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

    companion object {
        val values = listOf(minutes, seconds, milliseconds, microseconds, nanoseconds)
    }
}

fun parseJMHCLIParameter(s: String): JMHCLIArgs {
    if (s.isBlank()) {
        return JMHCLIArgs()
    }

    val splitted = s.split(" ")
    val array = splitted.toTypedArray()

    val cmd = CommandLine(JMHCLIArgs())
    val parsed = cmd.parse(*array)
    if (parsed.size != 1) {
        throw CommandLine.ParameterException(cmd, "Could not parse JMHCLIParamerters")
    }
    return parsed[0].getCommand()
}
