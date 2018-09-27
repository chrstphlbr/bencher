package ch.uzh.ifi.seal.bencher.execution

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.chronoUnit
import org.funktionale.either.Either
import java.time.Duration

class ConfigExecTimePredictor(
        private val configurator: BenchmarkConfigurator,
        private val considerMultipleJMHParams: Boolean = true
) : ExecTimePredictor {

    override fun execTime(bench: Benchmark): Either<String, Duration> {
        val eConf = configurator.config(bench)
        if (eConf.isLeft()) {
            return Either.left(eConf.left().get())
        }
        val conf = eConf.right().get()

        val wtu = conf.warmupTimeUnit
        if (wtu.isEmpty()) {
            return Either.left("No warmup time unit defined")
        }

        val wi = conf.warmupIterations
        if (wi < 0) {
            return Either.left("Warmup iterations is < 0 ($wi)")
        }

        val wt = conf.warmupTime
        if (wt < 0) {
            return Either.left("Warmup time is < 0 ($wt)")
        }

        val warmupTime = Duration.of((wi * wt).toLong(), wtu.get().chronoUnit)

        val mtu = conf.measurementTimeUnit
        if (mtu.isEmpty()) {
            return Either.left("No measurement time unit defined")
        }

        val mi = conf.measurementIterations
        if (mi < 0) {
            return Either.left("Measurement iterations is < 0 ($mi)")
        }

        val mt = conf.measurementTime
        if (mt < 0) {
            return Either.left("Measurement time is < 0 ($mt)")
        }

        val measurementTime = Duration.of((mi * mt).toLong(), mtu.get().chronoUnit)

        val f = conf.forks
        if (f < 0) {
            return Either.left("Forks is < 0 ($f)")
        }
        val rf = if (f == 0) { 1 } else { f }

        val wf = conf.warmupForks
        if (wf < 0) {
            return Either.left("Warmup forks is < 0 ($wf)")
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

        return Either.right(allParameterizationsTime)
    }

    private fun multiplyDuration(times: Int, duration: Duration): Duration =
            (0 until times).fold(Duration.ZERO) { sum, _ -> sum.plus(duration) }
}
