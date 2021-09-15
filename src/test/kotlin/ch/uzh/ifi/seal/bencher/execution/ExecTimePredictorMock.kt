package ch.uzh.ifi.seal.bencher.execution

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import java.time.Duration

class ExecTimePredictorMock(private val benchDurations: Map<Benchmark, Duration>) : ExecTimePredictor {
    override fun execTime(bench: Benchmark): Either<String, Duration> {
        val dur = benchDurations[bench]
        return if (dur == null) {
            Either.Left("No duration prediction for benchmark $bench")
        } else {
            Either.Right(dur)
        }
    }
}
