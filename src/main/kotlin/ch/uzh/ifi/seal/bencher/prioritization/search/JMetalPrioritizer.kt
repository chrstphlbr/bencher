package ch.uzh.ifi.seal.bencher.prioritization.search

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Version
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGOverlap
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGOverlapImpl
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.weight.MethodWeights
import ch.uzh.ifi.seal.bencher.measurement.*
import ch.uzh.ifi.seal.bencher.prioritization.PrioritizedMethod
import ch.uzh.ifi.seal.bencher.prioritization.Prioritizer

class JMetalPrioritizer(
    private val cgResult: CGResult,
    private val methodWeights: MethodWeights,
    performanceChanges: PerformanceChanges?,
    private val v1: Version,
    private val v2: Version,
) : Prioritizer {

    private val performanceChanges: PerformanceChanges
    private val overlap: CGOverlap

    init {
        this.performanceChanges = performanceChanges ?: noPerformanceChanges()
        this.overlap = CGOverlapImpl(cgResult.calls.map { it.value })
    }

    override fun prioritize(benchs: Iterable<Benchmark>): Either<String, List<PrioritizedMethod<Benchmark>>> {
        val cov = prepareCoverage(benchs)

        val problem = PrioritizationProblem(
            cgResult = cov,
            methodWeights = methodWeights,
            cgOverlap = overlap,
            performanceChanges = performanceChanges,
        )

        TODO("not implemented")
    }

    private fun prepareCoverage(benchs: Iterable<Benchmark>): CGResult =
        CGResult(
            calls = benchs
                .asSequence()
                .filter{ cgResult.calls[it] != null }
                .associateWith { cgResult.calls[it]!! }
        )

    private fun noPerformanceChanges(): PerformanceChanges =
        PerformanceChangesImpl(
            changes = cgResult.calls
                .asSequence()
                // assume that there are only Benchmark objects in there, otherwise a runtime exception is acceptable
                .map { (m, _) -> m as Benchmark }
                .map { noPerformanceChange(it)}
                .toList()
        )

    private fun noPerformanceChange(b: Benchmark): PerformanceChange =
        PerformanceChange(
            benchmark = b,
            v1 = v1,
            v2 = v2,
            type = PerformanceChangeType.NO,
            min = 0,
            max = 0,
        )
}
