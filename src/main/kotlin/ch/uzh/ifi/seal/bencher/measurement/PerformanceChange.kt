package ch.uzh.ifi.seal.bencher.measurement

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Version

data class PerformanceChange(
    val benchmark: Benchmark,
    val v1: Version,
    val v2: Version,
    val type: PerformanceChangeType,
    val min: Int,
    val max: Int
)

enum class PerformanceChangeType {
    IMPROVEMENT,
    REGRESSION,
    NO,
    OLD,
    NEW;

    companion object {
        private object StringRepresentation {
            const val improvement = "i"
            const val regression = "r"
            const val no = "no"
            const val old = "o"
            const val new = "n"
        }

        fun to(pct: PerformanceChangeType): String =
            when(pct) {
                IMPROVEMENT -> StringRepresentation.improvement
                REGRESSION -> StringRepresentation.regression
                NO -> StringRepresentation.no
                OLD -> StringRepresentation.old
                NEW -> StringRepresentation.new
            }

        fun from(str: String): Either<String, PerformanceChangeType> =
            when (str) {
                StringRepresentation.improvement -> Either.Right(IMPROVEMENT)
                StringRepresentation.regression -> Either.Right(REGRESSION)
                StringRepresentation.no -> Either.Right(NO)
                StringRepresentation.old -> Either.Right(OLD)
                StringRepresentation.new -> Either.Right(NEW)
                else -> Either.Left("invalid string representation '$str'")
            }
    }
}
