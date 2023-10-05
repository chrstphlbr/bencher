package ch.uzh.ifi.seal.bencher.execution

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.chronoUnit
import java.time.Duration

class ConfigExecTimePredictor(
        private val configurator: BenchmarkConfigurator,
        private val considerMultipleJMHParams: Boolean = true
) : ExecTimePredictor {

    override fun execTime(bench: Benchmark): Either<String, Duration> {
        val eConf = configurator.config(bench)
        val conf = eConf.getOrElse {
            return Either.Left(it)
        }

        val wi = conf.warmupIterations
        if (wi < 0) {
            return Either.Left("Warmup iterations is < 0 ($wi)")
        }

        val wt = conf.warmupTime
        if (wt < 0) {
            return Either.Left("Warmup time is < 0 ($wt)")
        }

        val wtu = conf.warmupTimeUnit
            .map { it.chronoUnit }
            .getOrElse {
                return Either.Left("No warmup time unit defined")
            }

        val warmupTime = Duration.of((wi * wt).toLong(), wtu)

        val mi = conf.measurementIterations
        if (mi < 0) {
            return Either.Left("Measurement iterations is < 0 ($mi)")
        }

        val mt = conf.measurementTime
        if (mt < 0) {
            return Either.Left("Measurement time is < 0 ($mt)")
        }

        val mtu = conf.measurementTimeUnit
            .map { it.chronoUnit }
            .getOrElse {
                return Either.Left("No measurement time unit defined")
            }

        val measurementTime = Duration.of((mi * mt).toLong(), mtu)

        val f = conf.forks
        if (f < 0) {
            return Either.Left("Forks is < 0 ($f)")
        }
        val rf = if (f == 0) {
            1
        } else {
            f
        }

        val wf = conf.warmupForks
        if (wf < 0) {
            return Either.Left("Warmup forks is < 0 ($wf)")
        }

        // time a single fork takes
        val oneForkTime = warmupTime + measurementTime

        // time all forks (forks + warmupForks) take
        val totalTime: Duration = multiplyDuration(rf + wf, oneForkTime)

        // time all configurations (JMH-parameterizations) of benchmark take
        val allParameterizationsTime = if (considerMultipleJMHParams) {
            val params = bench.jmhParams.let { params ->
                val s = params.size
                if (s == 0) {
                    1
                } else {
                    s
                }
            }
            multiplyDuration(params, totalTime)
        } else {
            totalTime
        }

        return Either.Right(allParameterizationsTime)
    }

    private fun multiplyDuration(times: Int, duration: Duration): Duration =
            (0 until times).fold(Duration.ZERO) { sum, _ -> sum.plus(duration) }
}
