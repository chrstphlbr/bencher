package ch.uzh.ifi.seal.bencher.analysis.finder

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper

class NoMethodFinderMock<T : Method> : MethodFinder<T> {
    override fun all(): Either<String, List<T>> = Either.Right(listOf())
}

class BenchFinderMock : MethodFinder<Benchmark> {
    override fun all(): Either<String, List<Benchmark>> =
            Either.Right(
                    listOf(
                            JarTestHelper.BenchParameterized.bench1,
                            JarTestHelper.BenchNonParameterized.bench2,
                            JarTestHelper.OtherBench.bench3,
                            JarTestHelper.BenchParameterized2v2.bench4
                    )
            )
}
