package ch.uzh.ifi.seal.bencher.measurement

import ch.uzh.ifi.seal.bencher.Version
import ch.uzh.ifi.seal.bencher.VersionPair
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper

object PerformanceChangesTestHelper {

    val versions = listOf(
        Version(1, 0, 0),
        Version(1, 0, 1),
        Version(1, 1, 0),
        Version(2, 0, 0),
    )

    val versionPairs = listOf(
        VersionPair(versions[0], versions[1]),
        VersionPair(versions[1], versions[2]),
        VersionPair(versions[2], versions[3]),
    )

    val bench1Changes = JarTestHelper.BenchParameterized.bench1.let { b ->
        listOf(
            versionPairs[0].let { PerformanceChange(b, it.v1, it.v2, PerformanceChangeType.NO, 0, 0) },
            versionPairs[1].let { PerformanceChange(b, it.v1, it.v2, PerformanceChangeType.IMPROVEMENT, 2, 10) },
            versionPairs[2].let { PerformanceChange(b, it.v1, it.v2, PerformanceChangeType.REGRESSION, 5, 7) },
        )
    }

    val bench1MinChangeSum = bench1Changes.sumOf { it.min } * 1.0
    val bench1MaxChangeSum = bench1Changes.sumOf { it.max } * 1.0

    val bench2Changes = JarTestHelper.BenchNonParameterized.bench2.let { b ->
        listOf(
            versionPairs[0].let { PerformanceChange(b, it.v1, it.v2, PerformanceChangeType.NO, 0, 0) },
            versionPairs[1].let { PerformanceChange(b, it.v1, it.v2, PerformanceChangeType.NO, 0, 0) },
            versionPairs[2].let { PerformanceChange(b, it.v1, it.v2, PerformanceChangeType.NO, 0, 0) },
        )
    }

    val bench2MinChangeSum = bench2Changes.sumOf { it.min } * 1.0
    val bench2MaxChangeSum = bench2Changes.sumOf { it.max } * 1.0

    val bench3Changes = JarTestHelper.OtherBench.bench3.let { b ->
        listOf(
            versionPairs[0].let { PerformanceChange(b, it.v1, it.v2, PerformanceChangeType.IMPROVEMENT, 2, 3) },
            versionPairs[1].let { PerformanceChange(b, it.v1, it.v2, PerformanceChangeType.IMPROVEMENT, 5, 10) },
            versionPairs[2].let { PerformanceChange(b, it.v1, it.v2, PerformanceChangeType.IMPROVEMENT, 4, 6) },
        )
    }

    val bench3MinChangeSum = bench3Changes.sumOf { it.min } * 1.0
    val bench3MaxChangeSum = bench3Changes.sumOf { it.max } * 1.0

    val bench4Changes = JarTestHelper.BenchParameterized2.bench4.let { b ->
        listOf(
            versionPairs[0].let { PerformanceChange(b, it.v1, it.v2, PerformanceChangeType.REGRESSION, 4, 5) },
            versionPairs[1].let { PerformanceChange(b, it.v1, it.v2, PerformanceChangeType.REGRESSION, 3, 7) },
            versionPairs[2].let { PerformanceChange(b, it.v1, it.v2, PerformanceChangeType.REGRESSION, 8, 10) },
        )
    }

    val bench4MinChangeSum = bench4Changes.sumOf { it.min } * 1.0
    val bench4MaxChangeSum = bench4Changes.sumOf { it.max } * 1.0

    val allChanges = listOf(
        bench1Changes,
        bench2Changes,
        bench3Changes,
        bench4Changes,
    ).flatten()

    val changes = PerformanceChangesImpl(allChanges)
}
