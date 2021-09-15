package ch.uzh.ifi.seal.bencher.execution

import arrow.core.Some
import ch.uzh.ifi.seal.bencher.JMHVersion
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.concurrent.TimeUnit

class DefaultExecConfigTest {

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 5, 10, 15, 20])
    fun pre121(minor: Int) {
        val v0 = JMHVersion(major = 0, minor = minor)
        val dc0 = defaultExecConfig(v0)
        Assertions.assertTrue(dc0 == expPre121)

        val v = JMHVersion(major = 1, minor = minor)
        val dc = defaultExecConfig(v)
        Assertions.assertTrue(dc == expPre121)
    }

    @ParameterizedTest
    @ValueSource(ints = [21, 22, 25, 30])
    fun post121Minor(minor: Int) {
        val v1 = JMHVersion(major = 1, minor = minor)
        val dc1 = defaultExecConfig(v1)
        Assertions.assertTrue(dc1 == expPost121)
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 3, 5, 10])
    fun post121Major(major: Int) {
        val v1 = JMHVersion(major = major, minor = 1)
        val dc1 = defaultExecConfig(v1)
        Assertions.assertTrue(dc1 == expPost121)
    }

    companion object {
        private val expPre121 = ExecutionConfiguration(
                warmupIterations = 20,
                warmupTime = 1,
                warmupTimeUnit = Some(TimeUnit.SECONDS),
                measurementIterations = 20,
                measurementTime = 1,
                measurementTimeUnit = Some(TimeUnit.SECONDS),
                forks = 10,
                warmupForks = 0,
                mode = listOf("Throughput"),
                outputTimeUnit = Some(TimeUnit.SECONDS)
        )

        private val expPost121 = ExecutionConfiguration(
                warmupIterations = 5,
                warmupTime = 10,
                warmupTimeUnit = Some(TimeUnit.SECONDS),
                measurementIterations = 5,
                measurementTime = 10,
                measurementTimeUnit = Some(TimeUnit.SECONDS),
                forks = 5,
                warmupForks = 0,
                mode = listOf("Throughput"),
                outputTimeUnit = Some(TimeUnit.SECONDS)
        )
    }
}
