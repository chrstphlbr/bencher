package ch.uzh.ifi.seal.bencher.measurement

import arrow.core.*
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.JmhID
import ch.uzh.ifi.seal.bencher.Version
import ch.uzh.ifi.seal.bencher.VersionPair

interface PerformanceChanges {
    fun benchmarks(): List<Benchmark>
    fun benchmarkChanges(b: Benchmark): Option<List<PerformanceChange>>
    fun benchmarkChangeStatistic(b: Benchmark, statistic: Statistic<Int, Double>, defaultValue: Option<Double> = None): Option<Double>

    fun versions(): List<VersionPair>
    fun versionChanges(v1: Version, v2: Version): Option<List<PerformanceChange>>

    fun changesUntilVersion(v: Version, untilVersion1: Boolean, including: Boolean): Option<List<PerformanceChange>>
}

class PerformanceChangesImpl(
    changes: Iterable<PerformanceChange>
) : PerformanceChanges {

    private val benchmarks: List<Benchmark>
    private val benchmarkChanges: Map<JmhID, List<PerformanceChange>>
    private val benchmarkChangesStatistics: MutableMap<String, MutableMap<JmhID, Double>> = mutableMapOf() // statistic name to map

    private val versions: List<VersionPair>
    private val versionChanges: Map<VersionPair, List<PerformanceChange>>

    init {
        val bs = mutableListOf<Benchmark>()
        val bcs = mutableMapOf<JmhID, MutableList<PerformanceChange>>()

        val vs = mutableListOf<VersionPair>()
        val vcs = mutableMapOf<VersionPair, MutableList<PerformanceChange>>()

        changes.forEach { pc ->
            val b = pc.benchmark
            bs.add(b)
            val bc = bcs.getOrPut(b.jmhID()) { mutableListOf() }
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

    override fun benchmarkChanges(b: Benchmark): Option<List<PerformanceChange>> = benchmarkChanges[b.jmhID()].toOption()

    override fun benchmarkChangeStatistic(b: Benchmark, statistic: Statistic<Int, Double>, defaultValue: Option<Double>): Option<Double> {
        val stMap = synchronized(benchmarkChangesStatistics) {
            benchmarkChangesStatistics.getOrPut(statistic.name) { mutableMapOf() }
        }

        val st = synchronized(stMap) {
            stMap.getOrPut(b.jmhID()) {
                val changes = benchmarkChanges(b)
                    .getOrElse { return defaultValue }
                    .map { it.min }

                statistic.statistic(changes)
            }
        }

        return Some(st)
    }

    override fun versions(): List<VersionPair> = versions

    override fun versionChanges(v1: Version, v2: Version): Option<List<PerformanceChange>> =
        versionChanges[Pair(v1, v2)].toOption()

    override fun changesUntilVersion(v: Version, untilVersion1: Boolean, including: Boolean): Option<List<PerformanceChange>> {
        val changes = versionChanges
            .asSequence()
            .filter { (versionPair, _) ->
                when {
                    untilVersion1 && including -> versionPair.first <= v
                    untilVersion1 && !including -> versionPair.first < v
                    !untilVersion1 && including -> versionPair.second <= v
                    else -> versionPair.second < v
                }
            }
            .map { (_, l) -> l }
            .flatten()
            .toList()

        return Some(changes)
    }
}
