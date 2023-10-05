package ch.uzh.ifi.seal.bencher.execution

import arrow.core.Some
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class ConfigBasedConfiguratorTest {

    private fun runParameterized(b: Benchmark, f: (b: Benchmark) -> Unit): Unit =
            b.parameterizedBenchmarks().forEach(f)

    private fun invalidConfigurator(b: Benchmark) {
        val c = ConfigBasedConfigurator(
                defaultExecConfig = ConfigurationTestHelper.unsetConfig,
                benchExecConfigs = mapOf(),
                classExecConfigs = mapOf()
        )

        val eConf = c.config(b)
        Assertions.assertTrue(eConf.isLeft())
    }

    @Test
    fun invalidConfiguratorMatch() {
        invalidConfigurator(JarTestHelper.BenchParameterized.bench1)
    }

    @Test
    fun invalidConfiguratorParam() {
        runParameterized(JarTestHelper.BenchParameterized.bench1, ::invalidConfigurator)
    }

    private fun emptyClassAndBenchConfig(b: Benchmark) {
        val c = ConfigBasedConfigurator(
            defaultExecConfig = ConfigurationTestHelper.defaultConfig,
            benchExecConfigs = mapOf(),
            classExecConfigs = mapOf()
        )

        val conf = c.config(b).getOrElse {
            Assertions.fail<String>("Could not retrieve config")
            return
        }

        Assertions.assertTrue(conf == ConfigurationTestHelper.defaultConfig)
    }

    @Test
    fun emptyClassAndBenchConfigMatch() {
        emptyClassAndBenchConfig(JarTestHelper.BenchParameterized.bench1)
    }

    @Test
    fun emptyClassAndBenchConfigParam() {
        runParameterized(JarTestHelper.BenchParameterized.bench1, ::emptyClassAndBenchConfig)
    }

    private fun fullBenchConfig(b: Benchmark) {
        val bc = ExecutionConfiguration(
            forks = 10,
            warmupForks = 11,
            warmupIterations = 12,
            warmupTime = 13,
            warmupTimeUnit = Some(TimeUnit.SECONDS),
            measurementIterations = 14,
            measurementTime = 15,
            measurementTimeUnit = Some(TimeUnit.DAYS),
            mode = listOf("AverageTime"),
            outputTimeUnit = Some(TimeUnit.HOURS)
        )

        val c = ConfigBasedConfigurator(
            defaultExecConfig = ConfigurationTestHelper.defaultConfig,
            benchExecConfigs = mapOf(Pair(b, bc)),
            classExecConfigs = mapOf()
        )

        val conf = c.config(b).getOrElse {
            Assertions.fail<String>("Could not retrieve config")
            return
        }

        Assertions.assertTrue(conf == bc)
    }

    @Test
    fun fullBenchConfigMatch() {
        fullBenchConfig(JarTestHelper.BenchParameterized.bench1)
    }

    @Test
    fun fullBenchConfigParam() {
        runParameterized(JarTestHelper.BenchParameterized.bench1, ::fullBenchConfig)
    }

    private fun fullClassConfig(b: Benchmark) {
        val cc = ExecutionConfiguration(
            forks = 10,
            warmupForks = 11,
            warmupIterations = 12,
            warmupTime = 13,
            warmupTimeUnit = Some(TimeUnit.SECONDS),
            measurementIterations = 14,
            measurementTime = 15,
            measurementTimeUnit = Some(TimeUnit.DAYS),
            mode = listOf("AverageTime"),
            outputTimeUnit = Some(TimeUnit.HOURS)
        )

        val c = ConfigBasedConfigurator(
            defaultExecConfig = ConfigurationTestHelper.defaultConfig,
            benchExecConfigs = mapOf(),
            classExecConfigs = mapOf(Pair(Class(name = JarTestHelper.BenchParameterized.fqn), cc))
        )

        val conf = c.config(b).getOrElse {
            Assertions.fail<String>("Could not retrieve config")
            return
        }

        Assertions.assertTrue(conf == cc)
    }

    @Test
    fun fullClassConfigMatch() {
        fullClassConfig(JarTestHelper.BenchParameterized.bench1)
    }

    @Test
    fun fullClassConfigParam() {
        runParameterized(JarTestHelper.BenchParameterized.bench1, ::fullClassConfig)
    }

    private fun someDefaultSomeClassSomeBenchmark(b: Benchmark) {
        val cc = ConfigurationTestHelper.unsetConfig.copy(
            warmupIterations = 10,
            warmupTime = 11,
            warmupTimeUnit = Some(TimeUnit.DAYS),
            mode = listOf("SampleTime")
        )

        val bc = ConfigurationTestHelper.unsetConfig.copy(
            measurementIterations = 20,
            measurementTime = 21,
            measurementTimeUnit = Some(TimeUnit.HOURS),
            outputTimeUnit = Some(TimeUnit.NANOSECONDS)
        )

        val c = ConfigBasedConfigurator(
            defaultExecConfig = ConfigurationTestHelper.defaultConfig,
            benchExecConfigs = mapOf(Pair(b, bc)),
            classExecConfigs = mapOf(Pair(Class(name = JarTestHelper.BenchParameterized.fqn), cc))
        )

        val conf = c.config(b).getOrElse {
            Assertions.fail<String>("Could not retrieve config")
            return
        }

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

    @Test
    fun someDefaultSomeClassSomeBenchmarkMatch() {
        someDefaultSomeClassSomeBenchmark(JarTestHelper.BenchParameterized.bench1)
    }

    @Test
    fun someDefaultSomeClassSomeBenchmarkParam() {
        runParameterized(JarTestHelper.BenchParameterized.bench1, ::someDefaultSomeClassSomeBenchmark)
    }
}
