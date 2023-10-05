package ch.uzh.ifi.seal.bencher.selection

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoveragesTestHelper
import org.junit.jupiter.api.Assertions

abstract class AbstractFullChangeSelectionTest {

    protected fun assertSelection(e: Either<String, Iterable<Benchmark>>): List<Benchmark> {
        return e.getOrElse {
            Assertions.fail<String>("Could not retrieve selection: $it")
            throw IllegalStateException("should not happen")
        }.toList()
    }

    companion object {
        internal val b1 = JarTestHelper.BenchParameterized
        internal val b2 = JarTestHelper.BenchNonParameterized
        internal val b3 = JarTestHelper.OtherBench
        internal val b4 = JarTestHelper.BenchParameterized2

        internal val b1Cov = CoveragesTestHelper.b1MethodCov
        internal val b2Cov = CoveragesTestHelper.b2MethodCov
        internal val b3Cov = CoveragesTestHelper.b3MethodCov
        internal val b4Cov = CoveragesTestHelper.b4MethodCov

        internal val fullCov = Coverages(mapOf(b1Cov, b2Cov, b3Cov, b4Cov))
        internal val emptyCov = Coverages(mapOf())
    }
}
