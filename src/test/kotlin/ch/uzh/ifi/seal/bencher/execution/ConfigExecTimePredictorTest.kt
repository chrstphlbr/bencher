package ch.uzh.ifi.seal.bencher.execution

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.funktionale.option.Option
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
                        ConfigurationTestHelper.defaultConfig.copy(warmupTimeUnit = Option.empty())
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
                        ConfigurationTestHelper.defaultConfig.copy(measurementTimeUnit = Option.empty())
                )
        )))
        val eExecTime9 = p9.execTime(b)
        Assertions.assertTrue(eExecTime9.isLeft())
    }

    @Test
    fun measurementsOnly() {
        val b = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ExecutionConfiguration(
                                measurementIterations = 20,
                                measurementTime = 1,
                                measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
                                forks = 1,
                                warmupForks = 0,
                                warmupIterations = 0,
                                warmupTime = 1,
                                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                                mode = listOf("Throughput"),
                                outputTimeUnit = Option.Some(TimeUnit.SECONDS)
                        )
                )
        )))

        val eExecTime = p.execTime(b)

        if (eExecTime.isLeft()) {
            Assertions.fail<String>("Could not predict execution time: ${eExecTime.left().get()}")
        }
        val execTime = eExecTime.right().get()
        val expected = Duration.ofSeconds(20)

        Assertions.assertTrue(execTime == expected, "Expected $expected; was $execTime")
    }

    @Test
    fun warmupsOnly() {
        val b = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ExecutionConfiguration(
                                measurementIterations = 0,
                                measurementTime = 1,
                                measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
                                forks = 1,
                                warmupForks = 0,
                                warmupIterations = 20,
                                warmupTime = 1,
                                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                                mode = listOf("Throughput"),
                                outputTimeUnit = Option.Some(TimeUnit.SECONDS)
                        )
                )
        )))

        val eExecTime = p.execTime(b)

        if (eExecTime.isLeft()) {
            Assertions.fail<String>("Could not predict execution time: ${eExecTime.left().get()}")
        }
        val execTime = eExecTime.right().get()
        val expected = Duration.ofSeconds(20)

        Assertions.assertTrue(execTime == expected, "Expected $expected; was $execTime")
    }

    @Test
    fun warmupsAndMeasurements() {
        val b = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ExecutionConfiguration(
                                measurementIterations = 1,
                                measurementTime = 20,
                                measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
                                forks = 1,
                                warmupForks = 0,
                                warmupIterations = 20,
                                warmupTime = 1,
                                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                                mode = listOf("Throughput"),
                                outputTimeUnit = Option.Some(TimeUnit.SECONDS)
                        )
                )
        )))

        val eExecTime = p.execTime(b)

        if (eExecTime.isLeft()) {
            Assertions.fail<String>("Could not predict execution time: ${eExecTime.left().get()}")
        }
        val execTime = eExecTime.right().get()
        val expected = Duration.ofSeconds(40)

        Assertions.assertTrue(execTime == expected, "Expected $expected; was $execTime")
    }

    @Test
    fun zeroOrOneForks() {
        val b = JarTestHelper.BenchNonParameterized.bench2
        val p0 = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ExecutionConfiguration(
                                measurementIterations = 1,
                                measurementTime = 20,
                                measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
                                forks = 0,
                                warmupForks = 0,
                                warmupIterations = 20,
                                warmupTime = 1,
                                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                                mode = listOf("Throughput"),
                                outputTimeUnit = Option.Some(TimeUnit.SECONDS)
                        )
                )
        )))

        val eExecTime0 = p0.execTime(b)

        if (eExecTime0.isLeft()) {
            Assertions.fail<String>("Could not predict execution time: ${eExecTime0.left().get()}")
        }
        val execTime0 = eExecTime0.right().get()

        val p1 = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ExecutionConfiguration(
                                measurementIterations = 1,
                                measurementTime = 20,
                                measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
                                forks = 1,
                                warmupForks = 0,
                                warmupIterations = 20,
                                warmupTime = 1,
                                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                                mode = listOf("Throughput"),
                                outputTimeUnit = Option.Some(TimeUnit.SECONDS)
                        )
                )
        )))

        val eExecTime1 = p1.execTime(b)

        if (eExecTime1.isLeft()) {
            Assertions.fail<String>("Could not predict execution time: ${eExecTime1.left().get()}")
        }
        val execTime1 = eExecTime1.right().get()

        Assertions.assertEquals(execTime0, execTime1, "Fork time for 0 forks and 1 forks expected to be equal")
    }

    @Test
    fun multipleForks() {
        val b = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ExecutionConfiguration(
                                measurementIterations = 1,
                                measurementTime = 20,
                                measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
                                forks = 10,
                                warmupForks = 0,
                                warmupIterations = 20,
                                warmupTime = 1,
                                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                                mode = listOf("Throughput"),
                                outputTimeUnit = Option.Some(TimeUnit.SECONDS)
                        )
                )
        )))

        val eExecTime = p.execTime(b)

        if (eExecTime.isLeft()) {
            Assertions.fail<String>("Could not predict execution time: ${eExecTime.left().get()}")
        }
        val execTime = eExecTime.right().get()
        // 10 forks * [(1 measurementIteration * 20s) + (20 warmupIterations * 1s)] = 10 * [1 * 20 + 20 * 1] = 400
        val expected = Duration.ofSeconds(400)

        Assertions.assertTrue(execTime == expected, "Expected $expected; was $execTime")
    }

    @Test
    fun warmupForks() {
        val b = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ExecutionConfiguration(
                                measurementIterations = 1,
                                measurementTime = 20,
                                measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
                                forks = 10,
                                warmupForks = 5,
                                warmupIterations = 20,
                                warmupTime = 1,
                                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                                mode = listOf("Throughput"),
                                outputTimeUnit = Option.Some(TimeUnit.SECONDS)
                        )
                )
        )))

        val eExecTime = p.execTime(b)

        if (eExecTime.isLeft()) {
            Assertions.fail<String>("Could not predict execution time: ${eExecTime.left().get()}")
        }
        val execTime = eExecTime.right().get()
        // (10 forks + 5 warmupForks) * [(1 measurementIteration * 20s) + (20 warmupIterations * 1s)] = 600
        val expected = Duration.ofSeconds(600)

        Assertions.assertTrue(execTime == expected, "Expected $expected; was $execTime")
    }

    @Test
    fun parameterizedBench() {
        val b = JarTestHelper.BenchParameterized.bench1
        val p = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b,
                        ExecutionConfiguration(
                                measurementIterations = 1,
                                measurementTime = 20,
                                measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
                                forks = 10,
                                warmupForks = 5,
                                warmupIterations = 20,
                                warmupTime = 1,
                                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                                mode = listOf("Throughput"),
                                outputTimeUnit = Option.Some(TimeUnit.SECONDS)
                        )
                )
        )))

        val eExecTime = p.execTime(b)

        if (eExecTime.isLeft()) {
            Assertions.fail<String>("Could not predict execution time: ${eExecTime.left().get()}")
        }
        val execTime = eExecTime.right().get()
        // times 3 JMH params
        // (3 JMH params) * (10 forks + 5 warmupForks) * [(1 measurementIteration * 20s) + (20 warmupIterations * 1s)] = 3 * 600 = 1800
        val expected = Duration.ofSeconds(1800)

        Assertions.assertTrue(execTime == expected, "Expected $expected; was $execTime")
    }

    @Test
    fun multipleBenchmarks() {
        val b1 = JarTestHelper.OtherBench.bench3
        val b2 = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b1,
                        ExecutionConfiguration(
                                measurementIterations = 1,
                                measurementTime = 20,
                                measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
                                forks = 10,
                                warmupForks = 5,
                                warmupIterations = 20,
                                warmupTime = 1,
                                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                                mode = listOf("Throughput"),
                                outputTimeUnit = Option.Some(TimeUnit.SECONDS)
                        )
                ),
                Pair(
                        b2,
                        ExecutionConfiguration(
                                warmupIterations = 5,
                                warmupTime = 10,
                                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                                measurementIterations = 5,
                                measurementTime = 10,
                                measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
                                forks = 5,
                                warmupForks = 0,
                                mode = listOf("Throughput"),
                                outputTimeUnit = Option.Some(TimeUnit.SECONDS)
                        )
                )
        )))

        val execTimes = p.execTimes(listOf(b1, b2))
        Assertions.assertTrue(execTimes.size == 2)

        // b1
        val eExecTimeB1 = execTimes[b1]
        if (eExecTimeB1 == null) {
            Assertions.fail<String>("No exec time for b1 ($b1)")
        }

        if (eExecTimeB1!!.isLeft()) {
            Assertions.fail<String>("Could not predict execution time for b1: ${eExecTimeB1.left().get()}")
        }
        val execTimeB1 = eExecTimeB1.right().get()
        // (10 forks + 5 warmupForks) * [(1 measurementIteration * 20s) + (20 warmupIterations * 1s)] = 600
        val expectedB1 = Duration.ofSeconds(600)

        Assertions.assertTrue(execTimeB1 == expectedB1, "Expected $expectedB1; was $execTimeB1")

        // b2
        val eExecTimeB2 = execTimes[b2]
        if (eExecTimeB2 == null) {
            Assertions.fail<String>("No exec time for b2 ($b2)")
        }

        if (eExecTimeB2!!.isLeft()) {
            Assertions.fail<String>("Could not predict execution time for b2: ${eExecTimeB2.left().get()}")
        }
        val execTimeB2 = eExecTimeB2.right().get()
        // (5 forks + 0 warmupForks) * [(5 measurementIteration * 10s) + (5 warmupIterations * 10s)] = 500
        val expectedB2 = Duration.ofSeconds(500)

        Assertions.assertTrue(execTimeB2 == expectedB2, "Expected $expectedB2; was $execTimeB2")
    }

    @Test
    fun totalExecTime() {
        val b1 = JarTestHelper.OtherBench.bench3
        val b2 = JarTestHelper.BenchNonParameterized.bench2
        val p = ConfigExecTimePredictor(configurator = BenchmarkConfiguratorMock(mapOf(
                Pair(
                        b1,
                        ExecutionConfiguration(
                                measurementIterations = 1,
                                measurementTime = 20,
                                measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
                                forks = 10,
                                warmupForks = 5,
                                warmupIterations = 20,
                                warmupTime = 1,
                                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                                mode = listOf("Throughput"),
                                outputTimeUnit = Option.Some(TimeUnit.SECONDS)
                        )
                ),
                Pair(
                        b2,
                        ExecutionConfiguration(
                                warmupIterations = 5,
                                warmupTime = 10,
                                warmupTimeUnit = Option.Some(TimeUnit.SECONDS),
                                measurementIterations = 5,
                                measurementTime = 10,
                                measurementTimeUnit = Option.Some(TimeUnit.SECONDS),
                                forks = 5,
                                warmupForks = 0,
                                mode = listOf("Throughput"),
                                outputTimeUnit = Option.Some(TimeUnit.SECONDS)
                        )
                )
        )))

        val etet = p.totalExecTime(listOf(b1, b2))
        if (etet.isLeft()) {
            Assertions.fail<String>("Could not get total execution time")
        }
        val tet = etet.right().get()

        // (10 forks + 5 warmupForks) * [(1 measurementIteration * 20s) + (20 warmupIterations * 1s)] = 600
        val expectedB1 = Duration.ofSeconds(600)
        // (5 forks + 0 warmupForks) * [(5 measurementIteration * 10s) + (5 warmupIterations * 10s)] = 500
        val expectedB2 = Duration.ofSeconds(500)

        val expTotal = expectedB1 + expectedB2

        Assertions.assertTrue(tet == expTotal, "Expected $expTotal; was $tet")
    }
}
