package ch.uzh.ifi.seal.bencher.measurement

import arrow.core.*
import ch.uzh.ifi.seal.bencher.Benchmark

interface PerformanceChanges {
    fun benchmarks(): List<Benchmark>
    fun benchmarkChanges(b: Benchmark): Option<List<PerformanceChange>>
    fun benchmarkChangeStatistic(b: Benchmark, statistic: Statistic<Int, Double>): Option<Double>

    fun versions(): List<VersionPair>
    fun versionChanges(v1: Version, v2: Version): Option<List<PerformanceChange>>
}

class PerformanceChangesImpl(
    private val changes: Iterable<PerformanceChange>
) : PerformanceChanges {

    val benchmarks: List<Benchmark>
    val benchmarkChanges: Map<Benchmark, List<PerformanceChange>>
    val benchmarkChangesStatistics: MutableMap<String, MutableMap<Benchmark, Double>> = mutableMapOf() // statistic name to map

    val versions: List<VersionPair>
    val versionChanges: Map<VersionPair, List<PerformanceChange>>

    init {
        val bs = mutableListOf<Benchmark>()
        val bcs = mutableMapOf<Benchmark, MutableList<PerformanceChange>>()

        val vs = mutableListOf<VersionPair>()
        val vcs = mutableMapOf<VersionPair, MutableList<PerformanceChange>>()

        changes.forEach { pc ->
            val b = pc.benchmark
            bs.add(b)
            val bc = bcs.getOrPut(b) { mutableListOf() }
            bc.add(pc)

            val v = Pair(pc.v1, pc.v2)
            vs.add(v)
            val vc = vcs.getOrPut(v) { mutableListOf() }
            vc.add(pc)
        }

        benchmarks = bs
        benchmarkChanges = bcs
        versions = vs
        versionChanges = vcs
    }

    override fun benchmarks(): List<Benchmark> = benchmarks

    override fun benchmarkChanges(b: Benchmark): Option<List<PerformanceChange>> = benchmarkChanges[b].toOption()

    override fun benchmarkChangeStatistic(b: Benchmark, statistic: Statistic<Int, Double>): Option<Double> {
        val stMap = synchronized(benchmarkChangesStatistics) {
            benchmarkChangesStatistics.getOrPut(statistic.name) { mutableMapOf() }
        }

        val st = synchronized(stMap) {
            stMap.getOrPut(b) {
                val changes = benchmarkChanges(b)
                    .getOrElse { return None }
                    .map { it.min }

                statistic.statistic(changes)
            }
        }

        return Some(st)
    }

    override fun versions(): List<VersionPair> = versions

    override fun versionChanges(v1: Version, v2: Version): Option<List<PerformanceChange>> =
        versionChanges[Pair(v1, v2)].toOption()
}
