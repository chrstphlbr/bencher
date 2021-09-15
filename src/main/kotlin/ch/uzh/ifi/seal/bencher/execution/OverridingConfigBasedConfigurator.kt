package ch.uzh.ifi.seal.bencher.execution

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Class

class OverridingConfigBasedConfigurator(
        private val overridingExecConfig: ExecutionConfiguration,
        defaultExecConfig: ExecutionConfiguration,
        classExecConfigs: Map<Class, ExecutionConfiguration>,
        benchExecConfigs: Map<Benchmark, ExecutionConfiguration>

) : ConfigBasedConfigurator(defaultExecConfig, classExecConfigs, benchExecConfigs) {
    override fun config(bench: Benchmark): Either<String, ExecutionConfiguration> {
        val ec = super.config(bench)
        return ec
            .mapLeft {
                "Invalid configuration for benchmark ($bench) and provided default/class/benchmark configurations"
            }
            .map {
                overridingExecConfig orDefault it
            }
    }
}
