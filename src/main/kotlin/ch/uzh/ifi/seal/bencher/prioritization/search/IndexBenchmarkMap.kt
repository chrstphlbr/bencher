package ch.uzh.ifi.seal.bencher.prioritization.search

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.MethodComparator

interface IndexBenchmarkMap {
    val size: Int
    operator fun get(idx: Int): Benchmark?
    operator fun get(b: Benchmark): Int?
    fun benchmarks(idxs: List<Int>): Either<String, List<Benchmark>>
    fun indices(benchmarks: List<Benchmark>): Either<String, List<Int>>
}

class IndexBenchmarkMapImpl(
    benchmarks: Iterable<Benchmark>,
    startAt: Int = 0
) : IndexBenchmarkMap {

    private val benchmarkIndexMap: Map<Benchmark, Int>
    private val indexBenchmarkMap: Map<Int, Benchmark>

    init {
        val sorted = benchmarks
            .toList()
            .sortedWith(MethodComparator::compare)

        val bi = mutableMapOf<Benchmark, Int>()
        val ib = mutableMapOf<Int, Benchmark>()

        sorted.forEachIndexed { i, b ->
            val idx = i + startAt
            bi[b] = idx
            ib[idx] = b
        }

        benchmarkIndexMap = bi
        indexBenchmarkMap = ib
    }

    override val size: Int
        get() = benchmarkIndexMap.size

    override fun get(idx: Int): Benchmark? = indexBenchmarkMap[idx]

    override fun get(b: Benchmark): Int? = benchmarkIndexMap[b]

    override fun benchmarks(idxs: List<Int>): Either<String, List<Benchmark>> =
        Either.Right(
            idxs.map { idx ->
                val b = indexBenchmarkMap[idx] ?: return Either.Left("no benchmark for index '$idx'")
                b
            }
        )

    override fun indices(benchmarks: List<Benchmark>): Either<String, List<Int>> =
        Either.Right(
            benchmarks.map { b ->
                val idx = benchmarkIndexMap[b] ?: return Either.Left("no index for benchnark '$b'")
                idx
            }
        )
}
