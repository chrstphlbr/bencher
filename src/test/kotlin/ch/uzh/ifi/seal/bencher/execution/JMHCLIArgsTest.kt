package ch.uzh.ifi.seal.bencher.execution

import arrow.core.Some
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import picocli.CommandLine
import java.util.concurrent.TimeUnit

class JMHCLIArgsTest {
    @Test
    fun none() {
        val cliString = ""
        val args = parseJMHCLIParameter(cliString)
        Assertions.assertEquals(unsetExecConfig.measurementIterations, args.measurementIterations)
        Assertions.assertEquals(unsetExecConfig.measurementTime, args.measurementTime)
        Assertions.assertEquals(null, args.measurementTimeUnit)
        Assertions.assertEquals(unsetExecConfig.warmupIterations, args.warmupIterations)
        Assertions.assertEquals(unsetExecConfig.warmupTime, args.warmupTime)
        Assertions.assertEquals(null, args.warmupTimeUnit)
        Assertions.assertEquals(unsetExecConfig.forks, args.forks)
        Assertions.assertEquals(unsetExecConfig.warmupForks, args.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, args.mode)
        Assertions.assertEquals(null, args.outputTimeUnit)

        val execConfig = args.execConfig()
        Assertions.assertEquals(unsetExecConfig.measurementIterations, execConfig.measurementIterations)
        Assertions.assertEquals(unsetExecConfig.measurementTime, execConfig.measurementTime)
        Assertions.assertEquals(unsetExecConfig.measurementTimeUnit, execConfig.measurementTimeUnit)
        Assertions.assertEquals(unsetExecConfig.warmupIterations, execConfig.warmupIterations)
        Assertions.assertEquals(unsetExecConfig.warmupTime, execConfig.warmupTime)
        Assertions.assertEquals(unsetExecConfig.warmupTimeUnit, execConfig.warmupTimeUnit)
        Assertions.assertEquals(unsetExecConfig.forks, execConfig.forks)
        Assertions.assertEquals(unsetExecConfig.warmupForks, execConfig.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, execConfig.mode)
        Assertions.assertEquals(unsetExecConfig.outputTimeUnit, execConfig.outputTimeUnit)
    }

    @Test
    fun measurementIterations() {
        val cliString = "-i 10"
        val args = parseJMHCLIParameter(cliString)
        Assertions.assertEquals(10, args.measurementIterations)
        Assertions.assertEquals(unsetExecConfig.measurementTime, args.measurementTime)
        Assertions.assertEquals(null, args.measurementTimeUnit)
        Assertions.assertEquals(unsetExecConfig.warmupIterations, args.warmupIterations)
        Assertions.assertEquals(unsetExecConfig.warmupTime, args.warmupTime)
        Assertions.assertEquals(null, args.warmupTimeUnit)
        Assertions.assertEquals(unsetExecConfig.forks, args.forks)
        Assertions.assertEquals(unsetExecConfig.warmupForks, args.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, args.mode)
        Assertions.assertEquals(null, args.outputTimeUnit)

        val execConfig = args.execConfig()
        Assertions.assertEquals(10, execConfig.measurementIterations)
        Assertions.assertEquals(unsetExecConfig.measurementTime, execConfig.measurementTime)
        Assertions.assertEquals(unsetExecConfig.measurementTimeUnit, execConfig.measurementTimeUnit)
        Assertions.assertEquals(unsetExecConfig.warmupIterations, execConfig.warmupIterations)
        Assertions.assertEquals(unsetExecConfig.warmupTime, execConfig.warmupTime)
        Assertions.assertEquals(unsetExecConfig.warmupTimeUnit, execConfig.warmupTimeUnit)
        Assertions.assertEquals(unsetExecConfig.forks, execConfig.forks)
        Assertions.assertEquals(unsetExecConfig.warmupForks, execConfig.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, execConfig.mode)
        Assertions.assertEquals(unsetExecConfig.outputTimeUnit, execConfig.outputTimeUnit)
    }

    @Test
    fun measurementTime() {
        val cliString = "-r 10"
        val args = parseJMHCLIParameter(cliString)
        Assertions.assertEquals(unsetExecConfig.measurementIterations, args.measurementIterations)
        Assertions.assertEquals(10, args.measurementTime)
        Assertions.assertEquals(null, args.measurementTimeUnit)
        Assertions.assertEquals(unsetExecConfig.warmupIterations, args.warmupIterations)
        Assertions.assertEquals(unsetExecConfig.warmupTime, args.warmupTime)
        Assertions.assertEquals(null, args.warmupTimeUnit)
        Assertions.assertEquals(unsetExecConfig.forks, args.forks)
        Assertions.assertEquals(unsetExecConfig.warmupForks, args.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, args.mode)
        Assertions.assertEquals(null, args.outputTimeUnit)

        val execConfig = args.execConfig()
        Assertions.assertEquals(unsetExecConfig.measurementIterations, execConfig.measurementIterations)
        Assertions.assertEquals(10, execConfig.measurementTime)
        Assertions.assertEquals(unsetExecConfig.measurementTimeUnit, execConfig.measurementTimeUnit)
        Assertions.assertEquals(unsetExecConfig.warmupIterations, execConfig.warmupIterations)
        Assertions.assertEquals(unsetExecConfig.warmupTime, execConfig.warmupTime)
        Assertions.assertEquals(unsetExecConfig.warmupTimeUnit, execConfig.warmupTimeUnit)
        Assertions.assertEquals(unsetExecConfig.forks, execConfig.forks)
        Assertions.assertEquals(unsetExecConfig.warmupForks, execConfig.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, execConfig.mode)
        Assertions.assertEquals(unsetExecConfig.outputTimeUnit, execConfig.outputTimeUnit)
    }

    @Test
    fun warmupIterations() {
        val cliString = "-wi 10"
        val args = parseJMHCLIParameter(cliString)
        Assertions.assertEquals(unsetExecConfig.measurementIterations, args.measurementIterations)
        Assertions.assertEquals(unsetExecConfig.measurementTime, args.measurementTime)
        Assertions.assertEquals(null, args.measurementTimeUnit)
        Assertions.assertEquals(10, args.warmupIterations)
        Assertions.assertEquals(unsetExecConfig.warmupTime, args.warmupTime)
        Assertions.assertEquals(null, args.warmupTimeUnit)
        Assertions.assertEquals(unsetExecConfig.forks, args.forks)
        Assertions.assertEquals(unsetExecConfig.warmupForks, args.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, args.mode)
        Assertions.assertEquals(null, args.outputTimeUnit)

        val execConfig = args.execConfig()
        Assertions.assertEquals(unsetExecConfig.measurementIterations, execConfig.measurementIterations)
        Assertions.assertEquals(unsetExecConfig.measurementTime, execConfig.measurementTime)
        Assertions.assertEquals(unsetExecConfig.measurementTimeUnit, execConfig.measurementTimeUnit)
        Assertions.assertEquals(10, execConfig.warmupIterations)
        Assertions.assertEquals(unsetExecConfig.warmupTime, execConfig.warmupTime)
        Assertions.assertEquals(unsetExecConfig.warmupTimeUnit, execConfig.warmupTimeUnit)
        Assertions.assertEquals(unsetExecConfig.forks, execConfig.forks)
        Assertions.assertEquals(unsetExecConfig.warmupForks, execConfig.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, execConfig.mode)
        Assertions.assertEquals(unsetExecConfig.outputTimeUnit, execConfig.outputTimeUnit)
    }

    @Test
    fun warmupTime() {
        val cliString = "-w 10"
        val args = parseJMHCLIParameter(cliString)
        Assertions.assertEquals(unsetExecConfig.measurementIterations, args.measurementIterations)
        Assertions.assertEquals(unsetExecConfig.measurementTime, args.measurementTime)
        Assertions.assertEquals(null, args.measurementTimeUnit)
        Assertions.assertEquals(unsetExecConfig.warmupIterations, args.warmupIterations)
        Assertions.assertEquals(10, args.warmupTime)
        Assertions.assertEquals(null, args.warmupTimeUnit)
        Assertions.assertEquals(unsetExecConfig.forks, args.forks)
        Assertions.assertEquals(unsetExecConfig.warmupForks, args.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, args.mode)
        Assertions.assertEquals(null, args.outputTimeUnit)

        val execConfig = args.execConfig()
        Assertions.assertEquals(unsetExecConfig.measurementIterations, execConfig.measurementIterations)
        Assertions.assertEquals(unsetExecConfig.measurementTime, execConfig.measurementTime)
        Assertions.assertEquals(unsetExecConfig.measurementTimeUnit, execConfig.measurementTimeUnit)
        Assertions.assertEquals(unsetExecConfig.warmupIterations, execConfig.warmupIterations)
        Assertions.assertEquals(10, execConfig.warmupTime)
        Assertions.assertEquals(unsetExecConfig.warmupTimeUnit, execConfig.warmupTimeUnit)
        Assertions.assertEquals(unsetExecConfig.forks, execConfig.forks)
        Assertions.assertEquals(unsetExecConfig.warmupForks, execConfig.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, execConfig.mode)
        Assertions.assertEquals(unsetExecConfig.outputTimeUnit, execConfig.outputTimeUnit)
    }

    @Test
    fun forks() {
        val cliString = "-f 10"
        val args = parseJMHCLIParameter(cliString)
        Assertions.assertEquals(unsetExecConfig.measurementIterations, args.measurementIterations)
        Assertions.assertEquals(unsetExecConfig.measurementTime, args.measurementTime)
        Assertions.assertEquals(null, args.measurementTimeUnit)
        Assertions.assertEquals(unsetExecConfig.warmupIterations, args.warmupIterations)
        Assertions.assertEquals(unsetExecConfig.warmupTime, args.warmupTime)
        Assertions.assertEquals(null, args.warmupTimeUnit)
        Assertions.assertEquals(10, args.forks)
        Assertions.assertEquals(unsetExecConfig.warmupForks, args.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, args.mode)
        Assertions.assertEquals(null, args.outputTimeUnit)

        val execConfig = args.execConfig()
        Assertions.assertEquals(unsetExecConfig.measurementIterations, execConfig.measurementIterations)
        Assertions.assertEquals(unsetExecConfig.measurementTime, execConfig.measurementTime)
        Assertions.assertEquals(unsetExecConfig.measurementTimeUnit, execConfig.measurementTimeUnit)
        Assertions.assertEquals(unsetExecConfig.warmupIterations, execConfig.warmupIterations)
        Assertions.assertEquals(unsetExecConfig.warmupTime, execConfig.warmupTime)
        Assertions.assertEquals(unsetExecConfig.warmupTimeUnit, execConfig.warmupTimeUnit)
        Assertions.assertEquals(10, execConfig.forks)
        Assertions.assertEquals(unsetExecConfig.warmupForks, execConfig.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, execConfig.mode)
        Assertions.assertEquals(unsetExecConfig.outputTimeUnit, execConfig.outputTimeUnit)
    }

    @Test
    fun warmupForks() {
        val cliString = "-wf 10"
        val args = parseJMHCLIParameter(cliString)
        Assertions.assertEquals(unsetExecConfig.measurementIterations, args.measurementIterations)
        Assertions.assertEquals(unsetExecConfig.measurementTime, args.measurementTime)
        Assertions.assertEquals(null, args.measurementTimeUnit)
        Assertions.assertEquals(unsetExecConfig.warmupIterations, args.warmupIterations)
        Assertions.assertEquals(unsetExecConfig.warmupTime, args.warmupTime)
        Assertions.assertEquals(null, args.warmupTimeUnit)
        Assertions.assertEquals(unsetExecConfig.forks, args.forks)
        Assertions.assertEquals(10, args.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, args.mode)
        Assertions.assertEquals(null, args.outputTimeUnit)

        val execConfig = args.execConfig()
        Assertions.assertEquals(unsetExecConfig.measurementIterations, execConfig.measurementIterations)
        Assertions.assertEquals(unsetExecConfig.measurementTime, execConfig.measurementTime)
        Assertions.assertEquals(unsetExecConfig.measurementTimeUnit, execConfig.measurementTimeUnit)
        Assertions.assertEquals(unsetExecConfig.warmupIterations, execConfig.warmupIterations)
        Assertions.assertEquals(unsetExecConfig.warmupTime, execConfig.warmupTime)
        Assertions.assertEquals(unsetExecConfig.warmupTimeUnit, execConfig.warmupTimeUnit)
        Assertions.assertEquals(unsetExecConfig.forks, execConfig.forks)
        Assertions.assertEquals(10, execConfig.warmupForks)
        Assertions.assertEquals(unsetExecConfig.mode, execConfig.mode)
        Assertions.assertEquals(unsetExecConfig.outputTimeUnit, execConfig.outputTimeUnit)
    }

    @Test
    fun mode() {
        val values = listOf("Throughput", "thrpt", "AverageTime", "avgt", "SampleTime", "sample", "SingleShotTime", "ss", "All", "all")

        values.forEach {
            val cliString = "-bm $it"
            val args = parseJMHCLIParameter(cliString)
            Assertions.assertEquals(unsetExecConfig.measurementIterations, args.measurementIterations)
            Assertions.assertEquals(unsetExecConfig.measurementTime, args.measurementTime)
            Assertions.assertEquals(null, args.measurementTimeUnit)
            Assertions.assertEquals(unsetExecConfig.warmupIterations, args.warmupIterations)
            Assertions.assertEquals(unsetExecConfig.warmupTime, args.warmupTime)
            Assertions.assertEquals(null, args.warmupTimeUnit)
            Assertions.assertEquals(unsetExecConfig.warmupForks, args.forks)
            Assertions.assertEquals(unsetExecConfig.warmupForks, args.warmupForks)
            Assertions.assertEquals(listOf(it), args.mode)
            Assertions.assertEquals(null, args.outputTimeUnit)

            val execConfig = args.execConfig()
            Assertions.assertEquals(unsetExecConfig.measurementIterations, execConfig.measurementIterations)
            Assertions.assertEquals(unsetExecConfig.measurementTime, execConfig.measurementTime)
            Assertions.assertEquals(unsetExecConfig.measurementTimeUnit, execConfig.measurementTimeUnit)
            Assertions.assertEquals(unsetExecConfig.warmupIterations, execConfig.warmupIterations)
            Assertions.assertEquals(unsetExecConfig.warmupTime, execConfig.warmupTime)
            Assertions.assertEquals(unsetExecConfig.warmupTimeUnit, execConfig.warmupTimeUnit)
            Assertions.assertEquals(unsetExecConfig.warmupForks, execConfig.forks)
            Assertions.assertEquals(unsetExecConfig.warmupForks, execConfig.warmupForks)
            Assertions.assertEquals(listOf(it), execConfig.mode)
            Assertions.assertEquals(unsetExecConfig.outputTimeUnit, execConfig.outputTimeUnit)
        }
    }

    @Test
    fun invalidMode() {
        val cliString = "-bm crazyMode"
        try {
            parseJMHCLIParameter(cliString)
            Assertions.fail<String>("Could parse CLI with '$cliString'")
        } catch (e: CommandLine.ParameterException) {
        }
    }

    private fun timeUnit(s: String): TimeUnit =
            when (s) {
                "m" -> TimeUnit.MINUTES
                "s" -> TimeUnit.SECONDS
                "ms" -> TimeUnit.MILLISECONDS
                "us" -> TimeUnit.MICROSECONDS
                "ns" -> TimeUnit.NANOSECONDS
                else -> throw IllegalArgumentException("Invalid time unit '$s'")
            }

    @Test
    fun outputTimeUnit() {
        val values = listOf("m", "s", "ms", "us", "ns")

        values.forEach {
            val cliString = "-tu $it"
            val args = parseJMHCLIParameter(cliString)
            Assertions.assertEquals(unsetExecConfig.measurementIterations, args.measurementIterations)
            Assertions.assertEquals(unsetExecConfig.measurementTime, args.measurementTime)
            Assertions.assertEquals(null, args.measurementTimeUnit)
            Assertions.assertEquals(unsetExecConfig.warmupIterations, args.warmupIterations)
            Assertions.assertEquals(unsetExecConfig.warmupTime, args.warmupTime)
            Assertions.assertEquals(null, args.warmupTimeUnit)
            Assertions.assertEquals(unsetExecConfig.warmupForks, args.forks)
            Assertions.assertEquals(unsetExecConfig.warmupForks, args.warmupForks)
            Assertions.assertEquals(unsetExecConfig.mode, args.mode)
            Assertions.assertEquals(timeUnit(it), args.outputTimeUnit)

            val execConfig = args.execConfig()
            Assertions.assertEquals(unsetExecConfig.measurementIterations, execConfig.measurementIterations)
            Assertions.assertEquals(unsetExecConfig.measurementTime, execConfig.measurementTime)
            Assertions.assertEquals(unsetExecConfig.measurementTimeUnit, execConfig.measurementTimeUnit)
            Assertions.assertEquals(unsetExecConfig.warmupIterations, execConfig.warmupIterations)
            Assertions.assertEquals(unsetExecConfig.warmupTime, execConfig.warmupTime)
            Assertions.assertEquals(unsetExecConfig.warmupTimeUnit, execConfig.warmupTimeUnit)
            Assertions.assertEquals(unsetExecConfig.warmupForks, execConfig.forks)
            Assertions.assertEquals(unsetExecConfig.warmupForks, execConfig.warmupForks)
            Assertions.assertEquals(unsetExecConfig.mode, execConfig.mode)
            Assertions.assertEquals(Some(timeUnit(it)), execConfig.outputTimeUnit)
        }
    }

    @Test
    fun invalidOutputTimeUnit() {
        val cliString = "-tu h"
        try {
            parseJMHCLIParameter(cliString)
            Assertions.fail<String>("Could parse CLI with '-tu h'")
        } catch (e: CommandLine.ParameterException) {
        }
    }

    @Test
    fun multipleParams() {
        val cliString = "-i 30 -r 2 -wi 15 -w 3 -wf 5 -f 10 -bm sample -tu m"
        val args = parseJMHCLIParameter(cliString)
        Assertions.assertEquals(30, args.measurementIterations)
        Assertions.assertEquals(2, args.measurementTime)
        Assertions.assertEquals(null, args.measurementTimeUnit)
        Assertions.assertEquals(15, args.warmupIterations)
        Assertions.assertEquals(3, args.warmupTime)
        Assertions.assertEquals(null, args.warmupTimeUnit)
        Assertions.assertEquals(10, args.forks)
        Assertions.assertEquals(5, args.warmupForks)
        Assertions.assertEquals(listOf("sample"), args.mode)
        Assertions.assertEquals(timeUnit("m"), args.outputTimeUnit)

        val execConfig = args.execConfig()
        Assertions.assertEquals(30, execConfig.measurementIterations)
        Assertions.assertEquals(2, execConfig.measurementTime)
        Assertions.assertEquals(unsetExecConfig.measurementTimeUnit, execConfig.measurementTimeUnit)
        Assertions.assertEquals(15, execConfig.warmupIterations)
        Assertions.assertEquals(3, execConfig.warmupTime)
        Assertions.assertEquals(unsetExecConfig.warmupTimeUnit, execConfig.warmupTimeUnit)
        Assertions.assertEquals(10, execConfig.forks)
        Assertions.assertEquals(5, execConfig.warmupForks)
        Assertions.assertEquals(listOf("sample"), execConfig.mode)
        Assertions.assertEquals(Some(timeUnit("m")), execConfig.outputTimeUnit)
    }
}