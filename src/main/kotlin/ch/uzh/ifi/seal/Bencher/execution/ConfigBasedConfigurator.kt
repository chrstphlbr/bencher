package ch.uzh.ifi.seal.bencher.execution

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Class
import org.funktionale.either.Either
import org.funktionale.option.Option
import org.funktionale.option.firstOption

class ConfigBasedConfigurator(
        private val defaultExecConfig: ExecutionConfiguration,
        private val classExecConfigs: Map<Class, ExecutionConfiguration>,
        private val benchExecConfigs: Map<Benchmark, ExecutionConfiguration>
) : BenchmarkConfigurator {

    override fun config(bench: Benchmark): Either<String, ExecutionConfiguration> {
        val c = benchmarkExecConfig(
                b = benchConfig(bench),
                c = classConfig(bench),
                d = defaultExecConfig
        )

        return if (valid(c)) {
            Either.right(c)
        } else {
            Either.left("Invalid configuration for benchmark ($bench) and provided default/class/benchmark configurations")
        }
    }

    private fun valid(c: ExecutionConfiguration): Boolean =
            c.forks >= 0 &&
                    c.warmupForks >= 0 &&
                    c.warmupIterations >= 0 &&
                    c.warmupTime >= 0 &&
                    c.warmupTimeUnit is Option.Some &&
                    c.measurementIterations >= 0 &&
                    c.measurementTime >= 0 &&
                    c.measurementTimeUnit is Option.Some

    private fun benchmarkExecConfig(b: ExecutionConfiguration, c: ExecutionConfiguration, d: ExecutionConfiguration): ExecutionConfiguration =
            b orDefault c  orDefault d

    private fun benchConfig(bench: Benchmark): ExecutionConfiguration =
            benchExecConfigs[bench] ?: unsetExecConfig

    private fun classConfig(bench: Benchmark): ExecutionConfiguration {
        val classConfig = classExecConfigs.toList().filter { (c, _) ->
            // Currently class-level JMH configurations are only inherited to directly containing benchmarks,
            // not to benchmarks in nested classes.
            // Therefore, a direct match on the class name is sufficient.
            bench.clazz == c.name
        }.firstOption()

        if (classConfig.isEmpty()) {
            return unsetExecConfig
        }
        return classConfig.get().second
    }
}
