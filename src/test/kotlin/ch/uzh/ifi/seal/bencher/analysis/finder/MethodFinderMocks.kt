package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.funktionale.either.Either

class NoMethodFinderMock : MethodFinder<Method> {
    override fun all(): Either<String, List<Method>> = Either.right(listOf())
}

class BenchFinderMock : MethodFinder<Benchmark> {
    override fun all(): Either<String, List<Benchmark>> =
            Either.right(
                    listOf(
                            JarTestHelper.BenchParameterized.bench1,
                            JarTestHelper.BenchNonParameterized.bench2,
                            JarTestHelper.OtherBench.bench3,
                            JarTestHelper.BenchParameterized2.bench4
                    )
            )
}
