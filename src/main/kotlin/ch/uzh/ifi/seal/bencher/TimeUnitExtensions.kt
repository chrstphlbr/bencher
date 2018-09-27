package ch.uzh.ifi.seal.bencher

import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

val TimeUnit.chronoUnit: ChronoUnit
    get() = when (this) {
        TimeUnit.NANOSECONDS -> ChronoUnit.NANOS
        TimeUnit.MICROSECONDS -> ChronoUnit.MICROS
        TimeUnit.MILLISECONDS -> ChronoUnit.MILLIS
        TimeUnit.SECONDS -> ChronoUnit.SECONDS
        TimeUnit.MINUTES -> ChronoUnit.MINUTES
        TimeUnit.HOURS -> ChronoUnit.HOURS
        TimeUnit.DAYS -> ChronoUnit.DAYS
        else -> throw AssertionError()
    }