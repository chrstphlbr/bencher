package ch.uzh.ifi.seal.bencher.execution

import arrow.core.None
import arrow.core.Some
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.TimeUnit

class ConfigExecTimePredictorTest {

    @Test
    fun noConfigForBenchmark() {
        val p = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf()))
        val eExecTime = p.execTime(JarTestHelper.BenchNonParameterized.bench2)
        Assertions.assertTrue(eExecTime.isLeft())
    }

    @Test
    fun invalidConfig() {
        val b = JarTestHelper.BenchNonParameterized.bench2
        val p1 = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ConfigurationTestHelper.unsetConfig
                )
        )))
        val eExecTime1 = p1.execTime(b)
        Assertions.assertTrue(eExecTime1.isLeft())

        val p2 = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ConfigurationTestHelper.defaultConfig.copy(forks = -1)
                )
        )))
        val eExecTime2 = p2.execTime(b)
        Assertions.assertTrue(eExecTime2.isLeft())

        val p3 = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ConfigurationTestHelper.defaultConfig.copy(warmupForks = -1)
                )
        )))
        val eExecTime3 = p3.execTime(b)
        Assertions.assertTrue(eExecTime3.isLeft())

        val p4 = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ConfigurationTestHelper.defaultConfig.copy(warmupIterations = -1)
                )
        )))
        val eExecTime4 = p4.execTime(b)
        Assertions.assertTrue(eExecTime4.isLeft())

        val p5 = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ConfigurationTestHelper.defaultConfig.copy(warmupTime = -1)
                )
        )))
        val eExecTime5 = p5.execTime(b)
        Assertions.assertTrue(eExecTime5.isLeft())


        val p6 = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ConfigurationTestHelper.defaultConfig.copy(warmupTimeUnit = None)
                )
        )))
        val eExecTime6 = p6.execTime(b)
        Assertions.assertTrue(eExecTime6.isLeft())

        val p7 = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ConfigurationTestHelper.defaultConfig.copy(measurementIterations = -1)
                )
        )))
        val eExecTime7 = p7.execTime(b)
        Assertions.assertTrue(eExecTime7.isLeft())

        val p8 = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ConfigurationTestHelper.defaultConfig.copy(measurementTime = -1)
                )
        )))
        val eExecTime8 = p8.execTime(b)
        Assertions.assertTrue(eExecTime8.isLeft())

        val p9 = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ConfigurationTestHelper.defaultConfig.copy(measurementTimeUnit = None)
                )
        )))
        val eExecTime9 = p9.execTime(b)
        Assertions.assertTrue(eExecTime9.isLeft())
    }

    @Test
    fun measurementsOnly() {
        val b = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(
            configurator = BenchmarkConfiguratorMock(
                mapOf(
                    Pair(
                        b,
                        ExecutionConfiguration(
                            measurementIterations = 20,
                            measurementTime = 1,
                            measurementTimeUnit = Some(TimeUnit.SECONDS),
                            forks = 1,
                            warmupForks = 0,
                            warmupIterations = 0,
                            warmupTime = 1,
                            warmupTimeUnit = Some(TimeUnit.SECONDS),
                            mode = listOf("Throughput"),
                            outputTimeUnit = Some(TimeUnit.SECONDS)
                        )
                    )
                )
            )
        )

        val execTime = p.execTime(b).getOrElse {
            Assertions.fail<String>("Could not predict execution time: $it")
            return
        }
        val expected = Duration.ofSeconds(20)

        Assertions.assertTrue(execTime == expected, "Expected $expected; was $execTime")
    }

    @Test
    fun warmupsOnly() {
        val b = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(
            configurator = BenchmarkConfiguratorMock(
                mapOf(
                    Pair(
                        b,
                        ExecutionConfiguration(
                            measurementIterations = 0,
                            measurementTime = 1,
                            measurementTimeUnit = Some(TimeUnit.SECONDS),
                            forks = 1,
                            warmupForks = 0,
                            warmupIterations = 20,
                            warmupTime = 1,
                            warmupTimeUnit = Some(TimeUnit.SECONDS),
                            mode = listOf("Throughput"),
                            outputTimeUnit = Some(TimeUnit.SECONDS)
                        )
                    )
                )
            )
        )

        val execTime = p.execTime(b).getOrElse {
            Assertions.fail<String>("Could not predict execution time: $it")
            return
        }
        val expected = Duration.ofSeconds(20)

        Assertions.assertTrue(execTime == expected, "Expected $expected; was $execTime")
    }

    @Test
    fun warmupsAndMeasurements() {
        val b = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(
            configurator = BenchmarkConfiguratorMock(
                mapOf(
                    Pair(
                        b,
                        ExecutionConfiguration(
                            measurementIterations = 1,
                            measurementTime = 20,
                            measurementTimeUnit = Some(TimeUnit.SECONDS),
                            forks = 1,
                            warmupForks = 0,
                            warmupIterations = 20,
                            warmupTime = 1,
                            warmupTimeUnit = Some(TimeUnit.SECONDS),
                            mode = listOf("Throughput"),
                            outputTimeUnit = Some(TimeUnit.SECONDS)
                        )
                    )
                )
            )
        )

        val execTime = p.execTime(b).getOrElse {
            Assertions.fail<String>("Could not predict execution time: $it")
            return
        }
        val expected = Duration.ofSeconds(40)

        Assertions.assertTrue(execTime == expected, "Expected $expected; was $execTime")
    }

    @Test
    fun zeroOrOneForks() {
        val b = JarTestHelper.BenchNonParameterized.bench2
        val p0 = ConfigExecTimePredictor(
            configurator = BenchmarkConfiguratorMock(
                mapOf(
                    Pair(
                        b,
                        ExecutionConfiguration(
                            measurementIterations = 1,
                            measurementTime = 20,
                            measurementTimeUnit = Some(TimeUnit.SECONDS),
                            forks = 0,
                            warmupForks = 0,
                            warmupIterations = 20,
                            warmupTime = 1,
                            warmupTimeUnit = Some(TimeUnit.SECONDS),
                            mode = listOf("Throughput"),
                            outputTimeUnit = Some(TimeUnit.SECONDS)
                        )
                    )
                )
            )
        )

        val execTime0 = p0.execTime(b).getOrElse {
            Assertions.fail<String>("Could not predict execution time: $it")
            return
        }

        val p1 = ConfigExecTimePredictor(
            configurator = BenchmarkConfiguratorMock(
                mapOf(
                    Pair(
                        b,
                        ExecutionConfiguration(
                            measurementIterations = 1,
                            measurementTime = 20,
                            measurementTimeUnit = Some(TimeUnit.SECONDS),
                            forks = 1,
                            warmupForks = 0,
                            warmupIterations = 20,
                            warmupTime = 1,
                            warmupTimeUnit = Some(TimeUnit.SECONDS),
                            mode = listOf("Throughput"),
                            outputTimeUnit = Some(TimeUnit.SECONDS)
                        )
                    )
                )
            )
        )

        val execTime1 = p1.execTime(b).getOrElse {
            Assertions.fail<String>("Could not predict execution time: $it")
            return
        }

        Assertions.assertEquals(execTime0, execTime1, "Fork time for 0 forks and 1 forks expected to be equal")
    }

    @Test
    fun multipleForks() {
        val b = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(
            configurator = BenchmarkConfiguratorMock(
                mapOf(
                    Pair(
                        b,
                        ExecutionConfiguration(
                            measurementIterations = 1,
                            measurementTime = 20,
                            measurementTimeUnit = Some(TimeUnit.SECONDS),
                            forks = 10,
                            warmupForks = 0,
                            warmupIterations = 20,
                            warmupTime = 1,
                            warmupTimeUnit = Some(TimeUnit.SECONDS),
                            mode = listOf("Throughput"),
                            outputTimeUnit = Some(TimeUnit.SECONDS)
                        )
                    )
                )
            )
        )

        val execTime = p.execTime(b).getOrElse {
            Assertions.fail<String>("Could not predict execution time: $it")
            return
        }
        // 10 forks * [(1 measurementIteration * 20s) + (20 warmupIterations * 1s)] = 10 * [1 * 20 + 20 * 1] = 400
        val expected = Duration.ofSeconds(400)

        Assertions.assertTrue(execTime == expected, "Expected $expected; was $execTime")
    }

    @Test
    fun warmupForks() {
        val b = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(
            configurator = BenchmarkConfiguratorMock(
                mapOf(
                    Pair(
                        b,
                        ExecutionConfiguration(
                            measurementIterations = 1,
                            measurementTime = 20,
                            measurementTimeUnit = Some(TimeUnit.SECONDS),
                            forks = 10,
                            warmupForks = 5,
                            warmupIterations = 20,
                            warmupTime = 1,
                            warmupTimeUnit = Some(TimeUnit.SECONDS),
                            mode = listOf("Throughput"),
                            outputTimeUnit = Some(TimeUnit.SECONDS)
                        )
                    )
                )
            )
        )

        val execTime = p.execTime(b).getOrElse {
            Assertions.fail<String>("Could not predict execution time: $it")
            return
        }
        // (10 forks + 5 warmupForks) * [(1 measurementIteration * 20s) + (20 warmupIterations * 1s)] = 600
        val expected = Duration.ofSeconds(600)

        Assertions.assertTrue(execTime == expected, "Expected $expected; was $execTime")
    }

    @Test
    fun parameterizedBench() {
        val b = JarTestHelper.BenchParameterized.bench1
        val p = ConfigExecTimePredictor(
            configurator = BenchmarkConfiguratorMock(
                mapOf(
                    Pair(
                        b,
                        ExecutionConfiguration(
                            measurementIterations = 1,
                            measurementTime = 20,
                            measurementTimeUnit = Some(TimeUnit.SECONDS),
                            forks = 10,
                            warmupForks = 5,
                            warmupIterations = 20,
                            warmupTime = 1,
                            warmupTimeUnit = Some(TimeUnit.SECONDS),
                            mode = listOf("Throughput"),
                            outputTimeUnit = Some(TimeUnit.SECONDS)
                        )
                    )
                )
            )
        )

        val execTime = p.execTime(b).getOrElse {
            Assertions.fail<String>("Could not predict execution time: $it")
            return
        }
        // times 3 JMH params
        // (3 JMH params) * (10 forks + 5 warmupForks) * [(1 measurementIteration * 20s) + (20 warmupIterations * 1s)] = 3 * 600 = 1800
        val expected = Duration.ofSeconds(1800)

        Assertions.assertTrue(execTime == expected, "Expected $expected; was $execTime")
    }

    @Test
    fun multipleBenchmarks() {
        val b1 = JarTestHelper.OtherBench.bench3
        val b2 = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(
            configurator = BenchmarkConfiguratorMock(
                mapOf(
                    Pair(
                        b1,
                        ExecutionConfiguration(
                            measurementIterations = 1,
                            measurementTime = 20,
                            measurementTimeUnit = Some(TimeUnit.SECONDS),
                            forks = 10,
                            warmupForks = 5,
                            warmupIterations = 20,
                            warmupTime = 1,
                            warmupTimeUnit = Some(TimeUnit.SECONDS),
                            mode = listOf("Throughput"),
                            outputTimeUnit = Some(TimeUnit.SECONDS)
                        )
                    ),
                    Pair(
                        b2,
                        ExecutionConfiguration(
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
                    )
                )
            )
        )

        val execTimes = p.execTimes(listOf(b1, b2))
        Assertions.assertTrue(execTimes.size == 2)

        // b1
        val eExecTimeB1 = execTimes[b1]
        if (eExecTimeB1 == null) {
            Assertions.fail<String>("No exec time for b1 ($b1)")
        }
        val execTimeB1 = eExecTimeB1!!.getOrElse {
            Assertions.fail<String>("Could not predict execution time for b1: $it")
            return
        }
        // (10 forks + 5 warmupForks) * [(1 measurementIteration * 20s) + (20 warmupIterations * 1s)] = 600
        val expectedB1 = Duration.ofSeconds(600)

        Assertions.assertTrue(execTimeB1 == expectedB1, "Expected $expectedB1; was $execTimeB1")

        // b2
        val eExecTimeB2 = execTimes[b2]
        if (eExecTimeB2 == null) {
            Assertions.fail<String>("No exec time for b2 ($b2)")
        }
        val execTimeB2 = eExecTimeB2!!.getOrElse {
            Assertions.fail<String>("Could not predict execution time for b2: $it")
            return
        }
        // (5 forks + 0 warmupForks) * [(5 measurementIteration * 10s) + (5 warmupIterations * 10s)] = 500
        val expectedB2 = Duration.ofSeconds(500)

        Assertions.assertTrue(execTimeB2 == expectedB2, "Expected $expectedB2; was $execTimeB2")
    }

    @Test
    fun totalExecTime() {
        val b1 = JarTestHelper.OtherBench.bench3
        val b2 = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(
            configurator = BenchmarkConfiguratorMock(
                mapOf(
                    Pair(
                        b1,
                        ExecutionConfiguration(
                            measurementIterations = 1,
                            measurementTime = 20,
                            measurementTimeUnit = Some(TimeUnit.SECONDS),
                            forks = 10,
                            warmupForks = 5,
                            warmupIterations = 20,
                            warmupTime = 1,
                            warmupTimeUnit = Some(TimeUnit.SECONDS),
                            mode = listOf("Throughput"),
                            outputTimeUnit = Some(TimeUnit.SECONDS)
                        )
                    ),
                    Pair(
                        b2,
                        ExecutionConfiguration(
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
                    )
                )
            )
        )

        val tet = p.totalExecTime(listOf(b1, b2)).getOrElse {
            Assertions.fail<String>("Could not get total execution time")
            return
        }

        // (10 forks + 5 warmupForks) * [(1 measurementIteration * 20s) + (20 warmupIterations * 1s)] = 600
        val expectedB1 = Duration.ofSeconds(600)
        // (5 forks + 0 warmupForks) * [(5 measurementIteration * 10s) + (5 warmupIterations * 10s)] = 500
        val expectedB2 = Duration.ofSeconds(500)

        val expTotal = expectedB1 + expectedB2

        Assertions.assertTrue(tet == expTotal, "Expected $expTotal; was $tet")
    }
}
