package ch.uzh.ifi.seal.bencher.execution

import org.funktionale.option.Option
import java.util.concurrent.TimeUnit

object ConfigurationTestHelper {

    val unsetConfig = ExecutionConfiguration(
            forks = -1,
            warmupForks = -1,
            warmupIterations = -1,
            warmupTime = -1,
            warmupTimeUnit = Option.empty(),
            measurementIterations = -1,
            measurementTime = -1,
            measurementTimeUnit = Option.empty()
    )

    val defaultConfig = ExecutionConfiguration(
            warmupIterations = 1,
            warmupTime = 1,
            warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
            measurementIterations = 1,
            measurementTime = 1,
            measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
            forks = 1,
            warmupForks = 1
    )
}