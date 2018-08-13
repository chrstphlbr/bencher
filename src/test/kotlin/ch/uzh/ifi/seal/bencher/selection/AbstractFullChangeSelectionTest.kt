package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGTestHelper
import org.funktionale.either.Either
import org.junit.jupiter.api.Assertions

abstract class AbstractFullChangeSelectionTest {

    protected fun assertSelection(e: Either<String, Iterable<Benchmark>>): List<Benchmark> {
        if (e.isLeft()) {
            Assertions.fail<String>("Could not retrieve selection: ${e.left().get()}")
        }
        return e.right().get().toList()
    }

    companion object {
        internal val b1 = JarTestHelper.BenchParameterized
        internal val b2 = JarTestHelper.BenchNonParameterized
        internal val b3 = JarTestHelper.OtherBench
        internal val b4 = JarTestHelper.BenchParameterized2

        internal val b1Cg = CGTestHelper.b1Cg
        internal val b2Cg = CGTestHelper.b2Cg
        internal val b3Cg = CGTestHelper.b3Cg
        internal val b4Cg = CGTestHelper.b4Cg

        internal val fullCg = CGResult(mapOf(b1Cg, b2Cg, b3Cg, b4Cg))
        internal val emptyCg = CGResult(mapOf())
    }
}
