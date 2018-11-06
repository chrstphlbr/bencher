package ch.uzh.ifi.seal.bencher.execution

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Class
import org.funktionale.either.Either

class OverridingConfigBasedConfigurator(
        private val overridingExecConfig: ExecutionConfiguration,
        defaultExecConfig: ExecutionConfiguration,
        classExecConfigs: Map<Class, ExecutionConfiguration>,
        benchExecConfigs: Map<Benchmark, ExecutionConfiguration>

) : ConfigBasedConfigurator(defaultExecConfig, classExecConfigs, benchExecConfigs) {
    override fun config(bench: Benchmark): Either<String, ExecutionConfiguration> {
        val ec = super.config(bench)

        return if (ec.isLeft()) {
            Either.left("Invalid configuration for benchmark ($bench) and provided default/class/benchmark configurations")
        } else {
            val c = overridingExecConfig orDefault ec.right().get()
            Either.right(c)
        }
    }
}