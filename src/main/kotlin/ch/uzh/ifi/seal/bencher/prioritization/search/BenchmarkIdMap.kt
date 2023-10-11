package ch.uzh.ifi.seal.bencher.prioritization.search

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.MethodComparator

interface BenchmarkIdMap {
    val size: Int
    operator fun get(id: Int): Benchmark?
    operator fun get(b: Benchmark): Int?
    fun benchmarks(ids: List<Int>): Either<String, List<Benchmark>>
    fun ids(benchmarks: List<Benchmark>): Either<String, List<Int>>
}

class BenchmarkIdMapImpl(
    benchmarks: Iterable<Benchmark>,
    startAt: Int = 0
) : BenchmarkIdMap {

    private val benchmarkIdMap: Map<Benchmark, Int>
    private val idBenchmarkMap: Map<Int, Benchmark>

    init {
        val sorted = benchmarks
            .toList()
            .sortedWith(MethodComparator::compare)

        val bi = mutableMapOf<Benchmark, Int>()
        val ib = mutableMapOf<Int, Benchmark>()

        sorted.forEachIndexed { i, b ->
            val id = i + startAt
            bi[b] = id
            ib[id] = b
        }

        benchmarkIdMap = bi
        idBenchmarkMap = ib
    }

    override val size: Int
        get() = benchmarkIdMap.size

    override fun get(id: Int): Benchmark? = idBenchmarkMap[id]

    override fun get(b: Benchmark): Int? = benchmarkIdMap[b]

    override fun benchmarks(ids: List<Int>): Either<String, List<Benchmark>> =
        Either.Right(
            ids.map { id ->
                val b = idBenchmarkMap[id] ?: return Either.Left("no benchmark for id '$id'")
                b
            }
        )

    override fun ids(benchmarks: List<Benchmark>): Either<String, List<Int>> =
        Either.Right(
            benchmarks.map { b ->
                val id = benchmarkIdMap[b] ?: return Either.Left("no id for benchmark '$b'")
                id
            }
        )
}
