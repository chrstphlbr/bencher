package ch.uzh.ifi.seal.bencher.measurement

import arrow.core.*
import ch.uzh.ifi.seal.bencher.*
import java.util.*

interface PerformanceChanges {
    fun benchmarks(): SortedSet<Benchmark>
    fun versions(): SortedSet<VersionPair>

    fun changes(b: Benchmark): Option<List<PerformanceChange>>
    fun changes(v1: Version, v2: Version): Option<List<PerformanceChange>>
    fun changesUntilVersion(v: Version, including: Boolean): Option<List<PerformanceChange>>

    fun benchmarkChangeStatistic(
        b: Benchmark,
        statistic: Statistic<Int, Double>,
        defaultValue: Option<Double> = None,
    ): Option<Double>
}

class PerformanceChangesImpl(
    changes: Iterable<PerformanceChange>
) : PerformanceChanges {

    private val benchmarks: SortedSet<Benchmark>
    private val benchmarkChanges: Map<JmhID, List<PerformanceChange>>
    // statistic name to map
    private val benchmarkChangesStatistics: MutableMap<String, MutableMap<JmhID, Double>> = mutableMapOf()

    private val versions: SortedSet<VersionPair>
    private val versionChanges: Map<VersionPair, List<PerformanceChange>>

    init {
        val bs = TreeSet<Benchmark>(MethodComparator)
        val bcs = mutableMapOf<JmhID, MutableList<PerformanceChange>>()

        val vs = TreeSet<VersionPair>()
        val vcs = mutableMapOf<VersionPair, MutableList<PerformanceChange>>()

        changes.forEach { pc ->
            val b = pc.benchmark
            bs.add(b)
            val bc = bcs.getOrPut(b.jmhID()) { mutableListOf() }
            bc.add(pc)

            val v = VersionPair(pc.v1, pc.v2)
            vs.add(v)
            val vc = vcs.getOrPut(v) { mutableListOf() }
            vc.add(pc)
        }

        benchmarks = bs
        benchmarkChanges = bcs
        versions = vs
        versionChanges = vcs
    }

    override fun benchmarks(): SortedSet<Benchmark> = benchmarks

    override fun changes(b: Benchmark): Option<List<PerformanceChange>> = benchmarkChanges[b.jmhID()].toOption()

    override fun benchmarkChangeStatistic(
        b: Benchmark,
        statistic: Statistic<Int, Double>,
        defaultValue: Option<Double>,
    ): Option<Double> {
        val stMap = synchronized(benchmarkChangesStatistics) {
            benchmarkChangesStatistics.getOrPut(statistic.name) { mutableMapOf() }
        }

        val st = synchronized(stMap) {
            stMap.getOrPut(b.jmhID()) {
                val changes = changes(b)
                    .getOrElse { return defaultValue }
                    .map { it.min }

                statistic.statistic(changes)
            }
        }

        return Some(st)
    }

    override fun versions(): SortedSet<VersionPair> = versions

    override fun changes(v1: Version, v2: Version): Option<List<PerformanceChange>> =
        versionChanges[VersionPair(v1, v2)].toOption()

    override fun changesUntilVersion(v: Version, including: Boolean): Option<List<PerformanceChange>> {
        val changes = versionChanges
            .asSequence()
            .filter { (versionPair, _) ->
                when (including) {
                    true -> versionPair.v2 <= v
                    false -> versionPair.v2 < v
                }
            }
            .map { (_, l) -> l }
            .flatten()
            .toList()

        return Some(changes)
    }
}
