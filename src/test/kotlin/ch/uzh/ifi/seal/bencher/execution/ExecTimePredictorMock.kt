package ch.uzh.ifi.seal.bencher.execution

import ch.uzh.ifi.seal.bencher.Benchmark
import org.funktionale.either.Either
import java.time.Duration

class ExecTimePredictorMock(private val benchDurations: Map<Benchmark, Duration>) : ExecTimePredictor {
    override fun execTime(bench: Benchmark): Either<String, Duration> {
        val dur = benchDurations[bench]
        return if (dur == null) {
            Either.left("No duration prediction for benchmark $bench")
        } else {
            Either.right(dur)
        }
    }
}
