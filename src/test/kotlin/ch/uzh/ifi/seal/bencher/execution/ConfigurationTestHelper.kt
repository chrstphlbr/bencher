package ch.uzh.ifi.seal.bencher.execution

import arrow.core.None
import arrow.core.Some
import java.util.concurrent.TimeUnit

object ConfigurationTestHelper {

    val unsetConfig = ExecutionConfiguration(
            forks = -1,
            warmupForks = -1,
            warmupIterations = -1,
            warmupTime = -1,
            warmupTimeUnit = None,
            measurementIterations = -1,
            measurementTime = -1,
            measurementTimeUnit = None,
            mode = listOf(),
            outputTimeUnit = None
    )

    val defaultConfig = ExecutionConfiguration(
            warmupIterations = 1,
            warmupTime = 1,
            warmupTimeUnit = Some(TimeUnit.SECONDS),
            measurementIterations = 1,
            measurementTime = 1,
            measurementTimeUnit = Some(TimeUnit.SECONDS),
            forks = 1,
            warmupForks = 1,
            mode = listOf("Throughput"),
            outputTimeUnit = Some(TimeUnit.SECONDS)
    )
}