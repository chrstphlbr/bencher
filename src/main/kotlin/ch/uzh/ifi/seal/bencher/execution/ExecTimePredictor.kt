package ch.uzh.ifi.seal.bencher.execution

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import java.time.Duration

interface ExecTimePredictor {
    // returns the execution time of a benchmark `bench` (right value)
    // or an error that occurreded during prediction
    fun execTime(bench: Benchmark): Either<String, Duration>

    // Returns a map of either
    //   (1) the execution time, or
    //   (2) an error that occurred while predicting the execution time
    // of benchmarks (`benchs`).
    fun execTimes(benchs: Iterable<Benchmark>): Map<Benchmark, Either<String, Duration>> =
        benchs.associateWith { b -> execTime(b) }

    // returns the total predicted execution time of a collection of benchmarks (`benchs`)
    // or an error that occurred during the total calculation (e.g., one of the benchmark's exec time could not be predicted)
    fun totalExecTime(benchs: Iterable<Benchmark>): Either<String, Duration> {
        var dur: Duration = Duration.ZERO

        for ((b, eDur) in execTimes(benchs)) {
            val d = eDur.getOrElse {
                return Either.Left("No execution time for benchmark ($b)")
            }
            dur += d
        }

        return Either.Right(dur)
    }
}
