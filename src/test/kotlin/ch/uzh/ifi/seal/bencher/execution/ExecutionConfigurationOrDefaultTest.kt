package ch.uzh.ifi.seal.bencher.execution

import arrow.core.Some
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class ExecutionConfigurationOrDefaultTest {

    @Test
    fun allDefault() {
        val c = unsetExecConfig
        val conf = c orDefault dec
        Assertions.assertTrue(conf == dec)
    }

    @Test
    fun allConfig() {
        val c = ExecutionConfiguration(
                warmupIterations = 2,
                warmupTime = 2,
                warmupTimeUnit = Some(TimeUnit.MINUTES),
                measurementTime = 2,
                measurementIterations = 2,
                measurementTimeUnit = Some(TimeUnit.MINUTES),
                forks = 2,
                warmupForks = 2,
                mode = listOf("AverageTime"),
                outputTimeUnit = Some(TimeUnit.MINUTES)
        )

        val conf = c orDefault dec
        Assertions.assertTrue(conf == c)
    }

    @Test
    fun setWi() {
        val c = unsetExecConfig.copy(warmupIterations = 2)
        val conf = c orDefault dec

        Assertions.assertTrue(conf.warmupIterations == 2)
        Assertions.assertTrue(conf.warmupTime == dec.warmupTime)
        Assertions.assertTrue(conf.warmupTimeUnit == dec.warmupTimeUnit)
        Assertions.assertTrue(conf.measurementIterations == dec.measurementIterations)
        Assertions.assertTrue(conf.measurementTime == dec.measurementTime)
        Assertions.assertTrue(conf.measurementTimeUnit == dec.measurementTimeUnit)
        Assertions.assertTrue(conf.forks == dec.forks)
        Assertions.assertTrue(conf.warmupForks == dec.warmupForks)
        Assertions.assertTrue(conf.mode == dec.mode)
        Assertions.assertTrue(conf.outputTimeUnit == dec.outputTimeUnit)
    }

    @Test
    fun setWt() {
        val c = unsetExecConfig.copy(warmupTime = 2)
        val conf = c orDefault dec

        Assertions.assertTrue(conf.warmupIterations == dec.warmupIterations)
        Assertions.assertTrue(conf.warmupTime == 2)
        Assertions.assertTrue(conf.warmupTimeUnit == dec.warmupTimeUnit)
        Assertions.assertTrue(conf.measurementIterations == dec.measurementIterations)
        Assertions.assertTrue(conf.measurementTime == dec.measurementTime)
        Assertions.assertTrue(conf.measurementTimeUnit == dec.measurementTimeUnit)
        Assertions.assertTrue(conf.forks == dec.forks)
        Assertions.assertTrue(conf.warmupForks == dec.warmupForks)
        Assertions.assertTrue(conf.mode == dec.mode)
        Assertions.assertTrue(conf.outputTimeUnit == dec.outputTimeUnit)
    }

    @Test
    fun setWtu() {
        val c = unsetExecConfig.copy(warmupTimeUnit = Some(TimeUnit.MINUTES))
        val conf = c orDefault dec

        Assertions.assertTrue(conf.warmupIterations == dec.warmupIterations)
        Assertions.assertTrue(conf.warmupTime == dec.warmupTime)
        Assertions.assertTrue(conf.warmupTimeUnit == Some(TimeUnit.MINUTES))
        Assertions.assertTrue(conf.measurementIterations == dec.measurementIterations)
        Assertions.assertTrue(conf.measurementTime == dec.measurementTime)
        Assertions.assertTrue(conf.measurementTimeUnit == dec.measurementTimeUnit)
        Assertions.assertTrue(conf.forks == dec.forks)
        Assertions.assertTrue(conf.warmupForks == dec.warmupForks)
        Assertions.assertTrue(conf.mode == dec.mode)
        Assertions.assertTrue(conf.outputTimeUnit == dec.outputTimeUnit)
    }

    @Test
    fun setMi() {
        val c = unsetExecConfig.copy(measurementIterations = 2)
        val conf = c orDefault dec

        Assertions.assertTrue(conf.warmupIterations == dec.warmupIterations)
        Assertions.assertTrue(conf.warmupTime == dec.warmupTime)
        Assertions.assertTrue(conf.warmupTimeUnit == dec.warmupTimeUnit)
        Assertions.assertTrue(conf.measurementIterations == 2)
        Assertions.assertTrue(conf.measurementTime == dec.measurementTime)
        Assertions.assertTrue(conf.measurementTimeUnit == dec.measurementTimeUnit)
        Assertions.assertTrue(conf.forks == dec.forks)
        Assertions.assertTrue(conf.warmupForks == dec.warmupForks)
        Assertions.assertTrue(conf.mode == dec.mode)
        Assertions.assertTrue(conf.outputTimeUnit == dec.outputTimeUnit)
    }

    @Test
    fun setMt() {
        val c = unsetExecConfig.copy(measurementTime = 2)
        val conf = c orDefault dec

        Assertions.assertTrue(conf.warmupIterations == dec.warmupIterations)
        Assertions.assertTrue(conf.warmupTime == dec.warmupTime)
        Assertions.assertTrue(conf.warmupTimeUnit == dec.warmupTimeUnit)
        Assertions.assertTrue(conf.measurementIterations == dec.measurementIterations)
        Assertions.assertTrue(conf.measurementTime == 2)
        Assertions.assertTrue(conf.measurementTimeUnit == dec.measurementTimeUnit)
        Assertions.assertTrue(conf.forks == dec.forks)
        Assertions.assertTrue(conf.warmupForks == dec.warmupForks)
        Assertions.assertTrue(conf.mode == dec.mode)
        Assertions.assertTrue(conf.outputTimeUnit == dec.outputTimeUnit)
    }

    @Test
    fun setMtu() {
        val c = unsetExecConfig.copy(measurementTimeUnit = Some(TimeUnit.MINUTES))
        val conf = c orDefault dec

        Assertions.assertTrue(conf.warmupIterations == dec.warmupIterations)
        Assertions.assertTrue(conf.warmupTime == dec.warmupTime)
        Assertions.assertTrue(conf.warmupTimeUnit == dec.warmupTimeUnit)
        Assertions.assertTrue(conf.measurementIterations == dec.measurementIterations)
        Assertions.assertTrue(conf.measurementTime == dec.measurementTime)
        Assertions.assertTrue(conf.measurementTimeUnit == Some(TimeUnit.MINUTES))
        Assertions.assertTrue(conf.forks == dec.forks)
        Assertions.assertTrue(conf.warmupForks == dec.warmupForks)
        Assertions.assertTrue(conf.mode == dec.mode)
        Assertions.assertTrue(conf.outputTimeUnit == dec.outputTimeUnit)
    }

    @Test
    fun setF() {
        val c = unsetExecConfig.copy(forks = 2)
        val conf = c orDefault dec

        Assertions.assertTrue(conf.warmupIterations == dec.warmupIterations)
        Assertions.assertTrue(conf.warmupTime == dec.warmupTime)
        Assertions.assertTrue(conf.warmupTimeUnit == dec.warmupTimeUnit)
        Assertions.assertTrue(conf.measurementIterations == dec.measurementIterations)
        Assertions.assertTrue(conf.measurementTime == dec.measurementTime)
        Assertions.assertTrue(conf.measurementTimeUnit == dec.measurementTimeUnit)
        Assertions.assertTrue(conf.forks == 2)
        Assertions.assertTrue(conf.warmupForks == dec.warmupForks)
        Assertions.assertTrue(conf.mode == dec.mode)
        Assertions.assertTrue(conf.outputTimeUnit == dec.outputTimeUnit)
    }

    @Test
    fun setWf() {
        val c = unsetExecConfig.copy(warmupForks = 2)
        val conf = c orDefault dec

        Assertions.assertTrue(conf.warmupIterations == dec.warmupIterations)
        Assertions.assertTrue(conf.warmupTime == dec.warmupTime)
        Assertions.assertTrue(conf.warmupTimeUnit == dec.warmupTimeUnit)
        Assertions.assertTrue(conf.measurementIterations == dec.measurementIterations)
        Assertions.assertTrue(conf.measurementTime == dec.measurementTime)
        Assertions.assertTrue(conf.measurementTimeUnit == dec.measurementTimeUnit)
        Assertions.assertTrue(conf.forks == dec.forks)
        Assertions.assertTrue(conf.warmupForks == 2)
        Assertions.assertTrue(conf.mode == dec.mode)
        Assertions.assertTrue(conf.outputTimeUnit == dec.outputTimeUnit)
    }

    @Test
    fun setMo() {
        val c = unsetExecConfig.copy(mode = listOf("AverageTime", "SampleTime"))
        val conf = c orDefault dec

        Assertions.assertTrue(conf.warmupIterations == dec.warmupIterations)
        Assertions.assertTrue(conf.warmupTime == dec.warmupTime)
        Assertions.assertTrue(conf.warmupTimeUnit == dec.warmupTimeUnit)
        Assertions.assertTrue(conf.measurementIterations == dec.measurementIterations)
        Assertions.assertTrue(conf.measurementTime == dec.measurementTime)
        Assertions.assertTrue(conf.measurementTimeUnit == dec.measurementTimeUnit)
        Assertions.assertTrue(conf.forks == dec.forks)
        Assertions.assertTrue(conf.warmupForks == dec.warmupForks)
        Assertions.assertTrue(conf.mode == listOf("AverageTime", "SampleTime"))
        Assertions.assertTrue(conf.outputTimeUnit == dec.outputTimeUnit)
    }

    @Test
    fun setOtu() {
        val c = unsetExecConfig.copy(outputTimeUnit = Some(TimeUnit.HOURS))
        val conf = c orDefault dec

        Assertions.assertTrue(conf.warmupIterations == dec.warmupIterations)
        Assertions.assertTrue(conf.warmupTime == dec.warmupTime)
        Assertions.assertTrue(conf.warmupTimeUnit == dec.warmupTimeUnit)
        Assertions.assertTrue(conf.measurementIterations == dec.measurementIterations)
        Assertions.assertTrue(conf.measurementTime == dec.measurementTime)
        Assertions.assertTrue(conf.measurementTimeUnit == dec.measurementTimeUnit)
        Assertions.assertTrue(conf.forks == dec.forks)
        Assertions.assertTrue(conf.warmupForks == dec.warmupForks)
        Assertions.assertTrue(conf.mode == dec.mode)
        Assertions.assertTrue(conf.outputTimeUnit == Some(TimeUnit.HOURS))
    }

    companion object {
        private val dec = ConfigurationTestHelper.defaultConfig
    }
}
