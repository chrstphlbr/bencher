package ch.uzh.ifi.seal.bencher.selection

import arrow.core.Either
import arrow.core.getOrHandle
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoveragesTestHelper
import org.junit.jupiter.api.Assertions

abstract class AbstractFullChangeSelectionTest {

    protected fun assertSelection(e: Either<String, Iterable<Benchmark>>): List<Benchmark> {
        return e.getOrHandle {
            Assertions.fail<String>("Could not retrieve selection: $it")
            throw IllegalStateException("should not happen")
        }.toList()
    }

    companion object {
        internal val b1 = JarTestHelper.BenchParameterized
        internal val b2 = JarTestHelper.BenchNonParameterized
        internal val b3 = JarTestHelper.OtherBench
        internal val b4 = JarTestHelper.BenchParameterized2

        internal val b1Cg = CoveragesTestHelper.b1Cov
        internal val b2Cg = CoveragesTestHelper.b2Cov
        internal val b3Cg = CoveragesTestHelper.b3Cov
        internal val b4Cg = CoveragesTestHelper.b4Cov

        internal val fullCg = Coverages(mapOf(b1Cg, b2Cg, b3Cg, b4Cg))
        internal val emptyCg = Coverages(mapOf())
    }
}
