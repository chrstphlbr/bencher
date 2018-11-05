package ch.uzh.ifi.seal.bencher.execution

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Class
import org.funktionale.either.Either

class OverridingConfigBasedConfigurator(
        private val overridingExecConfig: ExecutionConfiguration,
        private val defaultExecConfig: ExecutionConfiguration,
        classExecConfigs: Map<Class, ExecutionConfiguration>,
        benchExecConfigs: Map<Benchmark, ExecutionConfiguration>

) : ConfigBasedConfigurator(defaultExecConfig, classExecConfigs, benchExecConfigs) {
    override fun config(bench: Benchmark): Either<String, ExecutionConfiguration> {
        val c = benchmarkExecConfig(
                b = overridingExecConfig,
                c = benchConfig(bench),
                d = classConfig(bench),
                e = defaultExecConfig
        )

        return if (valid(c)) {
            Either.right(c)
        } else {
            Either.left("Invalid configuration for benchmark ($bench) and provided default/class/benchmark configurations")
        }
    }

    private fun benchmarkExecConfig(b: ExecutionConfiguration, c: ExecutionConfiguration, d: ExecutionConfiguration, e: ExecutionConfiguration): ExecutionConfiguration =
            b orDefault c orDefault d orDefault e
}
