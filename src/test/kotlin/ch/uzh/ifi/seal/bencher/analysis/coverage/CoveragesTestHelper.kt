package ch.uzh.ifi.seal.bencher.analysis.coverage

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.Coverage
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.Covered
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.PossiblyCovered
import ch.uzh.ifi.seal.bencher.fileResource
import java.io.File

object CoveragesTestHelper {

    val b1Coverage = JarTestHelper.BenchParameterized.bench1.let { b ->
        Pair(
                b,
                Coverage(
                        of = b,
                        unitResults = setOf(
                                PossiblyCovered(unit = JarTestHelper.CoreA.m, probability = 0.5, level = 1),
                                PossiblyCovered(unit = JarTestHelper.CoreB.m, probability = 0.5, level = 1),
                                PossiblyCovered(unit = JarTestHelper.CoreC.m, probability = 0.5, level = 2),
                                PossiblyCovered(unit = JarTestHelper.CoreE.mn1_1, probability = 0.25, level = 3),
                                PossiblyCovered(unit = JarTestHelper.CoreE.mn2, probability = 0.25, level = 3)
                        )
                )
        )
    }

    val b1Cov = b1Coverage

    private val b2Coverage = JarTestHelper.BenchNonParameterized.bench2.let { b ->
        Pair(
                b,
                Coverage(
                        of = b,
                        unitResults = setOf(
                                Covered(unit = JarTestHelper.CoreC.m, level = 1)
                        )
                )
        )
    }

    val b2Cov = b2Coverage

    private val b3Coverage = JarTestHelper.OtherBench.bench3.let { b ->
        Pair(
                b,
                Coverage(
                        of = b,
                        unitResults = setOf(
                                Covered(unit = JarTestHelper.CoreB.m, level = 1),
                                Covered(unit = JarTestHelper.CoreC.m, level = 2)
                        )
                )
        )
    }

    val b3Cov = b3Coverage

    private val b4Coverage = JarTestHelper.BenchParameterized2.bench4.let { b ->
        Pair(
                b,
                Coverage(
                        of = b,
                        unitResults = setOf(
                                PossiblyCovered(unit = JarTestHelper.CoreA.m, probability = 0.5, level = 1),
                                PossiblyCovered(unit = JarTestHelper.CoreD.m, probability = 0.5, level = 1)
                        )
                )
        )
    }

    val b4Cov = b4Coverage

    object PrinterReader {
        val b4 = Benchmark(
                clazz = "org.sample.Bench99",
                name = "bench99",
                params = listOf("org.openjdk.jmh.infra.Blackhole", "java.lang.String"),
                returnType = SourceCodeConstants.void,
                jmhParams = listOf(Pair("str", "1"), Pair("str", "2"))
        )

        val b4Coverage = Pair(
                b4,
                Coverage(
                        of = b4,
                        unitResults = setOf(
                                Covered(
                                        unit = PlainMethod(
                                                clazz = "org.sample.CoreZ",
                                                name = "m",
                                                params = listOf("java.lang.String", "int[][]"),
                                                returnType = SourceCodeConstants.void
                                        ),
                                        level = 1
                                )
                        )
                )
        )

        val b4Cov = b4Coverage

        val coverages = Coverages(mapOf(b1Cov, b2Cov, b3Cov, b4Cov))

        private val walaCovOutCoverage: File = "walaCgOutCG.txt".fileResource()

        private val walaCgOutCoverage: File = "walaCgOutCoverages.txt".fileResource()

        val covOut: File = walaCgOutCoverage
    }
}
