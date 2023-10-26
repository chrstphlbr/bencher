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
            const val IMPROVEMENT = "i"
            const val REGRESSION = "r"
            const val NO = "no"
            const val OLD = "o"
            const val NEW = "n"
        }

        fun toString(pct: PerformanceChangeType): String =
            when(pct) {
                IMPROVEMENT -> StringRepresentation.IMPROVEMENT
                REGRESSION -> StringRepresentation.REGRESSION
                NO -> StringRepresentation.NO
                OLD -> StringRepresentation.OLD
                NEW -> StringRepresentation.NEW
            }

        fun from(str: String): Either<String, PerformanceChangeType> =
            when (str) {
                StringRepresentation.IMPROVEMENT -> Either.Right(IMPROVEMENT)
                StringRepresentation.REGRESSION -> Either.Right(REGRESSION)
                StringRepresentation.NO -> Either.Right(NO)
                StringRepresentation.OLD -> Either.Right(OLD)
                StringRepresentation.NEW -> Either.Right(NEW)
                else -> Either.Left("invalid string representation '$str'")
            }
    }
}
