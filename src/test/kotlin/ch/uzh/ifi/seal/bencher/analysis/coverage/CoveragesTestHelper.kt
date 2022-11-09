package ch.uzh.ifi.seal.bencher.analysis.coverage

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Line
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.*
import ch.uzh.ifi.seal.bencher.fileResource
import java.io.File

object CoveragesTestHelper {

    private val b1MethodCoverage = JarTestHelper.BenchParameterized.bench1.let { b ->
        Pair(
                b,
                Coverage(
                        of = b,
                        unitResults = setOf(
                                PossiblyCovered(unit = JarTestHelper.CoreA.m.toCoverageUnit(), probability = 0.5, level = 1),
                                PossiblyCovered(unit = JarTestHelper.CoreB.m.toCoverageUnit(), probability = 0.5, level = 1),
                                PossiblyCovered(unit = JarTestHelper.CoreC.m.toCoverageUnit(), probability = 0.5, level = 2),
                                PossiblyCovered(unit = JarTestHelper.CoreE.mn1_1.toCoverageUnit(), probability = 0.25, level = 3),
                                PossiblyCovered(unit = JarTestHelper.CoreE.mn2.toCoverageUnit(), probability = 0.25, level = 3)
                        )
                )
        )
    }

    val b1MethodCov = b1MethodCoverage

    private val b2MethodCoverage = JarTestHelper.BenchNonParameterized.bench2.let { b ->
        Pair(
                b,
                Coverage(
                        of = b,
                        unitResults = setOf(
                                Covered(unit = JarTestHelper.CoreC.m.toCoverageUnit(), level = 1)
                        )
                )
        )
    }

    val b2MethodCov = b2MethodCoverage

    private val b3MethodCoverage = JarTestHelper.OtherBench.bench3.let { b ->
        Pair(
                b,
                Coverage(
                        of = b,
                        unitResults = setOf(
                                Covered(unit = JarTestHelper.CoreB.m.toCoverageUnit(), level = 1),
                                Covered(unit = JarTestHelper.CoreC.m.toCoverageUnit(), level = 2)
                        )
                )
        )
    }

    val b3MethodCov = b3MethodCoverage

    private val b4MethodCoverage = JarTestHelper.BenchParameterized2.bench4.let { b ->
        Pair(
                b,
                Coverage(
                        of = b,
                        unitResults = setOf(
                                PossiblyCovered(unit = JarTestHelper.CoreA.m.toCoverageUnit(), probability = 0.5, level = 1),
                                PossiblyCovered(unit = JarTestHelper.CoreD.m.toCoverageUnit(), probability = 0.5, level = 1)
                        )
                )
        )
    }

    val b4MethodCov = b4MethodCoverage

    object PrinterReader {

        private val b1LineCoverage = JarTestHelper.BenchParameterized.let { benchClass ->
            val bench = benchClass.bench1
            Pair(
                bench,
                Coverage(
                    of = bench,
                    unitResults = setOf(
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = benchClass.file, number = 2),
                                missedInstructions = 0,
                                coveredInstructions = 4,
                                missedBranches = 0,
                                coveredBranches = 0
                            ),
                            level = 1
                        ),
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = benchClass.file, number = 3),
                                missedInstructions = 0,
                                coveredInstructions = 10,
                                missedBranches = 0,
                                coveredBranches = 0
                            ),
                            level = 1
                        ),
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = JarTestHelper.CoreA.file, number = 2),
                                missedInstructions = 0,
                                coveredInstructions = 4,
                                missedBranches = 0,
                                coveredBranches = 0
                            ),
                            level = 1
                        ),
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = JarTestHelper.CoreA.file, number = 3),
                                missedInstructions = 0,
                                coveredInstructions = 10,
                                missedBranches = 0,
                                coveredBranches = 0
                            ),
                            level = 1
                        ),
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = JarTestHelper.CoreA.file, number = 4),
                                missedInstructions = 0,
                                coveredInstructions = 4,
                                missedBranches = null,
                                coveredBranches = null
                            ),
                            level = 1
                        ),
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = JarTestHelper.CoreB.file, number = 1),
                                missedInstructions = null,
                                coveredInstructions = null,
                                missedBranches = null,
                                coveredBranches = null
                            ),
                            level = 1
                        ),
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = JarTestHelper.CoreB.file, number = 2),
                                missedInstructions = null,
                                coveredInstructions = null,
                                missedBranches = null,
                                coveredBranches = null
                            ),
                            level = 1
                        ),
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = JarTestHelper.CoreC.file, number = 10),
                                missedInstructions = null,
                                coveredInstructions = null,
                                missedBranches = null,
                                coveredBranches = null
                            ),
                            level = 1
                        ),
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = JarTestHelper.CoreE.file, number = 1),
                                missedInstructions = null,
                                coveredInstructions = null,
                                missedBranches = null,
                                coveredBranches = null
                            ),
                            level = 1
                        ),
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = JarTestHelper.CoreE.file, number = 2),
                                missedInstructions = null,
                                coveredInstructions = null,
                                missedBranches = null,
                                coveredBranches = null
                            ),
                            level = 1
                        )
                    )
                )
            )
        }

        val b1LineCov = b1LineCoverage

        private val b2LineCoverage = JarTestHelper.BenchNonParameterized.let { benchClass ->
            val bench = benchClass.bench2
            Pair(
                bench,
                Coverage(
                    of = bench,
                    unitResults = setOf(
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = benchClass.file, number = 2),
                                missedInstructions = 2,
                                coveredInstructions = 3,
                                missedBranches = 1,
                                coveredBranches = 1
                            ),
                            level = 1
                        ),
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = benchClass.file, number = 3),
                                missedInstructions = 5,
                                coveredInstructions = 0,
                                missedBranches = 0,
                                coveredBranches = 0
                            ),
                            level = 1
                        ),
                        PossiblyCovered(
                            unit = CoverageUnitLine(
                                line = Line(file = JarTestHelper.CoreB.file, number = 10),
                                missedInstructions = 10,
                                coveredInstructions = 4,
                                missedBranches = 1,
                                coveredBranches = 1
                            ),
                            probability = 0.5,
                            level = 2
                        ),
                        PossiblyCovered(
                            unit = CoverageUnitLine(
                                line = Line(file = JarTestHelper.CoreC.file, number = 2),
                                missedInstructions = 0,
                                coveredInstructions = 14,
                                missedBranches = 1,
                                coveredBranches = 1
                            ),
                            probability = 0.5,
                            level = 3
                        )
                    )
                )
            )
        }

        val b2LineCov = b2LineCoverage

        private val b3LineCoverage = JarTestHelper.OtherBench.bench3.let { b ->
            Pair(
                b,
                Coverage(
                    of = b,
                    unitResults = setOf(
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = JarTestHelper.CoreB.file, number = 7),
                                missedInstructions = 1,
                                coveredInstructions = 7,
                                missedBranches = 0,
                                coveredBranches = 0
                            ),
                            level = -1
                        ),
                        Covered(
                            unit = CoverageUnitLine(
                                line = Line(file = JarTestHelper.CoreC.file, number = 4),
                                missedInstructions = 0,
                                coveredInstructions = 1,
                                missedBranches = 0,
                                coveredBranches = 1
                            ),
                            level = -1
                        ),
                    )
                )
            )
        }

        val b3LineCov = b3LineCoverage

        private val b4 = Benchmark(
                clazz = "org.sample.Bench99",
                name = "bench99",
                params = listOf("org.openjdk.jmh.infra.Blackhole", "java.lang.String"),
                returnType = SourceCodeConstants.void,
                jmhParams = listOf(Pair("str", "1"), Pair("str", "2"))
        )

        private val b4MethodCoverage = Pair(
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
                                        ).toCoverageUnit(),
                                        level = 1
                                )
                        )
                )
        )

        val b4MethodCov = b4MethodCoverage

        private val b4LineCoverage = Pair(
            b4,
            Coverage(
                of = b4,
                unitResults = setOf(
                    Covered(
                        unit = CoverageUnitLine(
                            line = Line(file = "Bench99.java", number = 2),
                            missedInstructions = 2,
                            coveredInstructions = 3,
                            missedBranches = 1,
                            coveredBranches = 1
                        ),
                        level = 1
                    ),
                    PossiblyCovered(
                        unit = CoverageUnitLine(
                            line = Line(file = JarTestHelper.CoreA.file, number = 3),
                            missedInstructions = 0,
                            coveredInstructions = 4,
                            missedBranches = 1,
                            coveredBranches = 0
                        ),
                        probability = 0.5,
                        level = 2
                    ),
                    PossiblyCovered(
                        unit = CoverageUnitLine(
                            line = Line(file = JarTestHelper.CoreA.file, number = 4),
                            missedInstructions = 4,
                            coveredInstructions = 0,
                            missedBranches = 0,
                            coveredBranches = 1
                        ),
                        probability = 0.2,
                        level = 3
                    )
                )
            )
        )

        val b4LineCov = b4LineCoverage

        val methodCoverages = Coverages(mapOf(b1MethodCov, b2MethodCov, b3MethodCov, b4MethodCov))

        val methodCoveragesOut: File = "methodCoveragesOut.txt".fileResource()

        val lineCoverages = Coverages(mapOf(b1LineCov, b2LineCov, b3LineCov, b4LineCov))

        val lineCoveragesOut: File = "lineCoveragesOut.txt".fileResource()

        val allCoverages = merge(methodCoverages, lineCoverages)

        val allCoveragesOut: File = "allCoveragesOut.txt".fileResource()
    }
}
