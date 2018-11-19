package ch.uzh.ifi.seal.bencher.execution

import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.funktionale.option.Option
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class ConfigBasedConfiguratorTest {

    @Test
    fun invalidConfigurator() {
        val c = ConfigBasedConfigurator(
                defaultExecConfig = ConfigurationTestHelper.unsetConfig,
                benchExecConfigs = mapOf(),
                classExecConfigs = mapOf()
        )

        val eConf = c.config(JarTestHelper.BenchParameterized.bench1)
        Assertions.assertTrue(eConf.isLeft())
    }

    @Test
    fun emptyClassAndBenchConfig() {
        val c = ConfigBasedConfigurator(
                defaultExecConfig = ConfigurationTestHelper.defaultConfig,
                benchExecConfigs = mapOf(),
                classExecConfigs = mapOf()
        )

        val eConf = c.config(JarTestHelper.BenchParameterized.bench1)
        if (eConf.isLeft()) {
            Assertions.fail<String>("Could not retrieve config")
        }
        val conf = eConf.right().get()

        Assertions.assertTrue(conf == ConfigurationTestHelper.defaultConfig)
    }

    @Test
    fun fullBenchConfig() {
        val b = JarTestHelper.BenchParameterized.bench1
        val bc = ExecutionConfiguration(
                forks = 10,
                warmupForks = 11,
                warmupIterations = 12,
                warmupTime = 13,
                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                measurementIterations = 14,
                measurementTime = 15,
                measurementTimeUnit = Option.Some(TimeUnit.DAYS),
                mode = listOf("AverageTime"),
                outputTimeUnit = Option.Some(TimeUnit.HOURS)
        )

        val c = ConfigBasedConfigurator(
                defaultExecConfig = ConfigurationTestHelper.defaultConfig,
                benchExecConfigs = mapOf(Pair(b, bc)),
                classExecConfigs = mapOf()
        )

        val eConf = c.config(b)
        if (eConf.isLeft()) {
            Assertions.fail<String>("Could not retrieve config")
        }
        val conf = eConf.right().get()

        Assertions.assertTrue(conf == bc)
    }

    @Test
    fun fullClassConfig() {
        val b = JarTestHelper.BenchParameterized.bench1
        val cc = ExecutionConfiguration(
                forks = 10,
                warmupForks = 11,
                warmupIterations = 12,
                warmupTime = 13,
                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                measurementIterations = 14,
                measurementTime = 15,
                measurementTimeUnit = Option.Some(TimeUnit.DAYS),
                mode = listOf("AverageTime"),
                outputTimeUnit = Option.Some(TimeUnit.HOURS)
        )

        val c = ConfigBasedConfigurator(
                defaultExecConfig = ConfigurationTestHelper.defaultConfig,
                benchExecConfigs = mapOf(),
                classExecConfigs = mapOf(Pair(Class(name = JarTestHelper.BenchParameterized.fqn), cc))
        )

        val eConf = c.config(b)
        if (eConf.isLeft()) {
            Assertions.fail<String>("Could not retrieve config")
        }
        val conf = eConf.right().get()

        Assertions.assertTrue(conf == cc)
    }

    @Test
    fun someDefaultSomeClassSomeBenchmark() {
        val b = JarTestHelper.BenchParameterized.bench1

        val cc = ConfigurationTestHelper.unsetConfig.copy(
                warmupIterations = 10,
                warmupTime = 11,
                warmupTimeUnit = Option.Some(TimeUnit.DAYS),
                mode = listOf("SampleTime")
        )

        val bc = ConfigurationTestHelper.unsetConfig.copy(
                measurementIterations = 20,
                measurementTime = 21,
                measurementTimeUnit = Option.Some(TimeUnit.HOURS),
                outputTimeUnit = Option.Some(TimeUnit.NANOSECONDS)
        )

        val c = ConfigBasedConfigurator(
                defaultExecConfig = ConfigurationTestHelper.defaultConfig,
                benchExecConfigs = mapOf(Pair(b, bc)),
                classExecConfigs = mapOf(Pair(Class(name = JarTestHelper.BenchParameterized.fqn), cc))
        )

        val eConf = c.config(b)
        if (eConf.isLeft()) {
            Assertions.fail<String>("Could not retrieve config")
        }
        val conf = eConf.right().get()

        val expectedConfig = ExecutionConfiguration(
                forks = ConfigurationTestHelper.defaultConfig.forks,
                warmupForks = ConfigurationTestHelper.defaultConfig.warmupForks,
                warmupIterations = cc.warmupIterations,
                warmupTime = cc.warmupTime,
                warmupTimeUnit = cc.warmupTimeUnit,
                measurementIterations = bc.measurementIterations,
                measurementTime = bc.measurementTime,
                measurementTimeUnit = bc.measurementTimeUnit,
                mode = cc.mode,
                outputTimeUnit = bc.outputTimeUnit
        )

        Assertions.assertTrue(conf == expectedConfig)
    }

}