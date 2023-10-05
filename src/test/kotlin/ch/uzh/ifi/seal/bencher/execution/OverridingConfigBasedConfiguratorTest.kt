package ch.uzh.ifi.seal.bencher.execution

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.JMHVersion
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OverridingConfigBasedConfiguratorTest {

    @Test
    fun invalidConfigurator() {
        val c = OverridingConfigBasedConfigurator(
                overridingExecConfig = defaultExecConfig(JMHVersion(1, 20)),
                defaultExecConfig = ConfigurationTestHelper.unsetConfig,
                benchExecConfigs = mapOf(),
                classExecConfigs = mapOf()
        )

        val eConf = c.config(JarTestHelper.BenchParameterized.bench1)
        Assertions.assertTrue(eConf.isLeft())
    }

    @Test
    fun emptyOverridingClassAndBenchConfig() {
        val c = OverridingConfigBasedConfigurator(
            overridingExecConfig = ConfigurationTestHelper.unsetConfig,
            defaultExecConfig = ConfigurationTestHelper.defaultConfig,
            benchExecConfigs = mapOf(),
            classExecConfigs = mapOf()
        )

        val conf = c.config(JarTestHelper.BenchParameterized.bench1).getOrElse {
            Assertions.fail<String>("Could not retrieve config")
            return
        }

        Assertions.assertTrue(conf == ConfigurationTestHelper.defaultConfig)
    }

    @Test
    fun overridingConfig() {
        val jmhVersion = JMHVersion(1, 20)

        val o = ConfigurationTestHelper.unsetConfig.copy(
            forks = 21
        )

        val b = JarTestHelper.BenchParameterized.bench1

        val default = defaultExecConfig(jmhVersion)

        val c = OverridingConfigBasedConfigurator(
            overridingExecConfig = o,
            defaultExecConfig = default,
            benchExecConfigs = mapOf(),
            classExecConfigs = mapOf()
        )

        val conf = c.config(b).getOrElse {
            Assertions.fail<String>("Could not retrieve config")
            return
        }

        Assertions.assertTrue(conf == default.copy(forks = o.forks))
    }

    @Test
    fun fullConfig() {
        val jmhVersion = JMHVersion(1, 20)

        val o = ConfigurationTestHelper.unsetConfig.copy(
            forks = 21
        )

        val b = JarTestHelper.BenchParameterized.bench1
        val bc = ConfigurationTestHelper.unsetConfig.copy(
            forks = 22,
            warmupForks = 23
        )

        val cc = ConfigurationTestHelper.unsetConfig.copy(
            warmupForks = 24,
            measurementTime = 25
        )

        val default = defaultExecConfig(jmhVersion)

        val c = OverridingConfigBasedConfigurator(
            overridingExecConfig = o,
            defaultExecConfig = default,
            benchExecConfigs = mapOf(Pair(b, bc)),
            classExecConfigs = mapOf(Pair(Class(name = JarTestHelper.BenchParameterized.fqn), cc))
        )

        val conf = c.config(b).getOrElse {
            Assertions.fail<String>("Could not retrieve config")
            return
        }

        Assertions.assertTrue(
            conf == default.copy(
                forks = o.forks,
                warmupForks = bc.warmupForks,
                measurementTime = cc.measurementTime
            )
        )
    }
}