package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.execution.ConfigurationTestHelper
import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration
import org.funktionale.option.Option
import org.junit.jupiter.api.Assertions
import java.util.concurrent.TimeUnit

abstract class AbstractAsmBenchExecInfoTest {

    protected fun assertClassConfig(cs: Map<Class, ExecutionConfiguration>, c: Class, ec: ExecutionConfiguration) {
        val cc = cs[c]
        if (cc == null) {
            Assertions.fail<String>("No class configuration in provided map for $c")
        }
        Assertions.assertTrue(cc == ec, "Execution configuration did not match expectation for $c.\nExpected: $ec\nWas $cc")
    }

    protected fun assertClassConfigs(cs: Map<Class, ExecutionConfiguration>) {
        assertClassConfig(cs, expClassNestedBenchmark.first, expClassNestedBenchmark.second)
        assertClassConfig(cs, expClassBench1.first, expClassBench1.second)
        assertClassConfig(cs, expClassBench3.first, expClassBench3.second)
        assertClassConfig(cs, expClassBench32.first, expClassBench32.second)
    }

    protected fun assertBenchConfig(cs: Map<Benchmark, ExecutionConfiguration>, b: Benchmark, ec: ExecutionConfiguration) {
        val bc = cs[b]
        if (bc == null) {
            Assertions.fail<String>("No benchmark configuration in provided map for $b")
        }
        Assertions.assertTrue(bc == ec, "Execution configuration did not match expectation for $b.\nExpected: $ec\nWas $bc")
    }

    protected fun assertBenchConfigs(cs: Map<Benchmark, ExecutionConfiguration>) {
        assertBenchConfig(cs, expBench11.first, expBench11.second)
        assertBenchConfig(cs, expBench12.first, expBench12.second)
        assertBenchConfig(cs, expBench2.first, expBench2.second)
        assertBenchConfig(cs, expBench31.first, expBench31.second)
        assertBenchConfig(cs, expBench321.first, expBench321.second)
    }

    companion object {

        // class configurations
        @JvmStatic
        protected val expClassNestedBenchmark = Pair(
                Class(name = JarTestHelper.NestedBenchmark.fqn),
                ExecutionConfiguration(
                        forks = 2,
                        warmupForks = 2,
                        measurementIterations = 10,
                        measurementTime = 2000,
                        measurementTimeUnit = Option.Some(TimeUnit.MILLISECONDS),
                        warmupIterations = 10,
                        warmupTime = 2000,
                        warmupTimeUnit = Option.Some(TimeUnit.MILLISECONDS),
                        mode = listOf("AverageTime"),
                        outputTimeUnit = Option.Some(TimeUnit.MILLISECONDS)
                )
        )

        @JvmStatic
        protected val expClassBench1 = Pair(
                Class(name = JarTestHelper.NestedBenchmark.Bench1.fqn),
                ConfigurationTestHelper.unsetConfig.copy(
                        forks = 10,
                        mode = listOf("SampleTime"),
                        outputTimeUnit = Option.Some(TimeUnit.NANOSECONDS)
                )
        )

        @JvmStatic
        protected val expClassBench3 = Pair(
                Class(name = JarTestHelper.NestedBenchmark.Bench3.fqn),
                ConfigurationTestHelper.unsetConfig.copy(
                        warmupIterations = 2,
                        warmupTime = 100,
                        warmupTimeUnit = Option.Some(TimeUnit.MILLISECONDS)
                )
        )

        @JvmStatic
        protected val expClassBench32 = Pair(
                Class(name = JarTestHelper.NestedBenchmark.Bench3.Bench32.fqn),
                ConfigurationTestHelper.unsetConfig.copy()
        )


        // benchmark configurations
        @JvmStatic
        protected val expBench2 = Pair(
                JarTestHelper.NestedBenchmark.bench2,
                ConfigurationTestHelper.unsetConfig.copy(
                        forks = 5,
                        measurementIterations = 50,
                        measurementTime = 500,
                        measurementTimeUnit = Option.Some(TimeUnit.MILLISECONDS),
                        mode = listOf("SampleTime")
                )
        )

        @JvmStatic
        protected val expBench11 = Pair(
                JarTestHelper.NestedBenchmark.Bench1.bench11,
                ConfigurationTestHelper.unsetConfig.copy(
                        warmupIterations = 20,
                        warmupTime = 100,
                        warmupTimeUnit = Option.Some(TimeUnit.MILLISECONDS),
                        measurementIterations = 10
                )
        )

        @JvmStatic
        protected val expBench12 = Pair(
                JarTestHelper.NestedBenchmark.Bench1.bench12,
                ConfigurationTestHelper.unsetConfig.copy()
        )

        @JvmStatic
        protected val expBench31 = Pair(
                JarTestHelper.NestedBenchmark.Bench3.bench31,
                ConfigurationTestHelper.unsetConfig.copy(
                        warmupForks = 10,
                        mode = listOf("Throughput", "SampleTime"),
                        outputTimeUnit = Option.Some(TimeUnit.MICROSECONDS)
                )
        )

        @JvmStatic
        protected val expBench321 = Pair(
                JarTestHelper.NestedBenchmark.Bench3.Bench32.bench321,
                ConfigurationTestHelper.unsetConfig.copy(
                        mode = listOf("SingleShotTime")
                )
        )
    }
}
