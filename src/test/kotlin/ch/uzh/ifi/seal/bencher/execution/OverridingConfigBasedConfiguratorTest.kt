package ch.uzh.ifi.seal.bencher.execution

import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.JMHVersion
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OverridingConfigBasedConfiguratorTest {

    @Test
    fun benchConfig() {
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

        val eConf = c.config(b)
        if (eConf.isLeft()) {
            Assertions.fail<String>("Could not retrieve config")
        }
        val conf = eConf.right().get()

        Assertions.assertTrue(conf == default.copy(forks = o.forks, warmupForks = bc.warmupForks, measurementTime = cc.measurementTime))
    }
}