package ch.uzh.ifi.seal.bencher.analysis.coverage.dyn.jacoco

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Line
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.*
import ch.uzh.ifi.seal.bencher.analysis.coverage.merge
import org.junit.jupiter.api.Assertions

object DCTestHelper {

    private fun emptyCoverage(b: Benchmark): Pair<Benchmark, Coverage> = Pair(
            b,
            Coverage(of = b, unitResults = setOf())
    )

    fun checkCovResult(coverages: Coverages, m: Method, ecs: List<Covered>) {
        val cs = coverages.coverages[m]
        if (cs == null) {
            Assertions.fail<String>("method $m has coverage")
            return
        }

        Assertions.assertEquals(m, cs.of)

        val s = cs.all().toList().size
        Assertions.assertEquals(ecs.size, s)

        ecs.forEach {
            val r = cs.single(m, it.unit)
            Assertions.assertEquals(it.copy(), r)
        }
    }

    object BenchParameterized {
        private fun bench1MethodCoverage(b: Benchmark): Pair<Benchmark, Coverage> {
            val pb = b.toPlainMethod()
            return Pair(
                b,
                Coverage(
                    of = b,
                    unitResults = setOf(
                        Covered(unit = JarTestHelper.BenchParameterized.constructor.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.BenchParameterized.setup.toPlainMethod().toCoverageUnit(), level = -1),
                        Covered(unit = pb.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreA.constructor.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreA.m.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreB.constructor.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreB.m.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreC.constructor.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreC.m.toCoverageUnit(), level = -1)
                    )
                )
            )
        }

        private fun bench1MethodCov(b: Benchmark): Pair<Benchmark, Coverage> = bench1MethodCoverage(b)

        val bench1MethodCovs: Array<Pair<Benchmark, Coverage>> = JarTestHelper.BenchParameterized.bench1.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { bench1MethodCov(it) }.toTypedArray()
        }

        val bench1MethodNonParamCovs: Array<Pair<Benchmark, Coverage>> =
                arrayOf(bench1MethodCov(JarTestHelper.BenchParameterized.bench1))

        private fun bench1LineCoverage(b: Benchmark): Pair<Benchmark, Coverage> {
            return Pair(
                b,
                Coverage(
                    of = b,
                    unitResults = setOf(
                        coveredLine(JarTestHelper.BenchParameterized, 41, 2),
                        coveredLine(JarTestHelper.BenchParameterized, 43, 4),
                        coveredLine(JarTestHelper.BenchParameterized, 50, 19),
                        coveredLine(JarTestHelper.BenchParameterized, 51, 1),
                        coveredLine(JarTestHelper.BenchParameterized, 55, 3),
                        coveredLine(JarTestHelper.BenchParameterized, 56, 3),
                        coveredLine(JarTestHelper.BenchParameterized, 57, 1)
                    ) + coreALineCoverages + coreBLineCoverages + coreCLineCoverages
                )
            )
        }

        private fun bench1LineCov(b: Benchmark): Pair<Benchmark, Coverage> = bench1LineCoverage(b)

        val bench1LineCovs: Array<Pair<Benchmark, Coverage>> = JarTestHelper.BenchParameterized.bench1.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { bench1LineCov(it) }.toTypedArray()
        }

        val bench1LineNonParamCovs: Array<Pair<Benchmark, Coverage>> =
            arrayOf(bench1LineCov(JarTestHelper.BenchParameterized.bench1))
    }

    object BenchNonParameterized {
        private val bench2MethodCoverage: Pair<Benchmark, Coverage> = JarTestHelper.BenchNonParameterized.bench2.let { b ->
            val pb = b.toPlainMethod()
            Pair(
                b,
                Coverage(
                    of = b,
                    unitResults = setOf(
                        Covered(unit = JarTestHelper.BenchNonParameterized.constructor.toCoverageUnit(), level = -1),
                        Covered(unit = pb.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreC.constructor.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreC.m.toCoverageUnit(), level = -1)
                    )
                )
            )
        }

        val bench2MethodCov: Pair<Benchmark, Coverage> = bench2MethodCoverage

        private val bench2LineCoverage: Pair<Benchmark, Coverage> = JarTestHelper.BenchNonParameterized.let { benchClass ->
            val bench = benchClass.bench2
            Pair(
                bench,
                Coverage(
                    of = bench,
                    unitResults = setOf(
                        coveredLine(benchClass, 40, 2),
                        coveredLine(benchClass, 42, 3),
                        coveredLine(benchClass, 44, 8),
                        coveredLine(benchClass, 48, 3),
                        coveredLine(benchClass, 49, 1)
                    ) + coreCLineCoverages
                )
            )
        }

        val bench2LineCov: Pair<Benchmark, Coverage> = bench2LineCoverage
    }

    object OtherBench {
        private val bench3MethodCoverage: Pair<Benchmark, Coverage> = JarTestHelper.OtherBench.bench3.let { b ->
            val pb = b.toPlainMethod()
            Pair(
                b,
                Coverage(
                    of = b,
                    unitResults = setOf(
                        Covered(unit = JarTestHelper.OtherBench.constructor.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.OtherBench.setup.toPlainMethod().toCoverageUnit(), level = -1),
                        Covered(unit = pb.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.OtherBench.tearDown.toPlainMethod().toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreB.constructor.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreB.m.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreC.constructor.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreC.m.toCoverageUnit(), level = -1)
                    )
                )
            )
        }

        val bench3MethodCov: Pair<Benchmark, Coverage> = bench3MethodCoverage

        private val bench3LineCoverage: Pair<Benchmark, Coverage> = JarTestHelper.OtherBench.let { benchClass ->
            val bench = benchClass.bench3
            Pair(
                bench,
                Coverage(
                    of = bench,
                    unitResults = setOf(
                        coveredLine(benchClass, 39, 2),
                        coveredLine(benchClass, 41, 4),
                        coveredLine(benchClass, 46, 12),
                        coveredLine(benchClass, 47, 1),
                        coveredLine(benchClass, 51, 3),
                        coveredLine(benchClass, 52, 1),
                        coveredLine(benchClass, 56, 3),
                        coveredLine(benchClass, 57, 1)
                    ) + coreBLineCoverages + coreCLineCoverages
                )
            )
        }

        val bench3LineCov: Pair<Benchmark, Coverage> = bench3LineCoverage
    }

    object BenchParameterized2 {
        private fun bench4MethodCoverage(b: Benchmark): Pair<Benchmark, Coverage> {
            val pb = b.toPlainMethod()
            return Pair(
                b,
                Coverage(
                    of = b,
                    unitResults = setOf(
                        Covered(unit = JarTestHelper.BenchParameterized2.constructor.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.BenchParameterized2.setup.toPlainMethod().toCoverageUnit(), level = -1),
                        Covered(unit = pb.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreA.constructor.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreA.m.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreD.constructor.toCoverageUnit(), level = -1),
                        Covered(unit = JarTestHelper.CoreD.m.toCoverageUnit(), level = -1)
                    )
                )
            )
        }

        internal fun bench4MethodCov(b: Benchmark): Pair<Benchmark, Coverage> = bench4MethodCoverage(b)

        val bench4MethodCovs: Array<Pair<Benchmark, Coverage>> = JarTestHelper.BenchParameterized2.bench4.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { bench4MethodCov(it) }.toTypedArray()
        }

        val bench4MethodNonParamCovs: Array<Pair<Benchmark, Coverage>> =
                arrayOf(bench4MethodCov(JarTestHelper.BenchParameterized2.bench4))

        private fun bench4LineCoverage(b: Benchmark): Pair<Benchmark, Coverage> {
            val benchClass = JarTestHelper.BenchParameterized2
            return Pair(
                b,
                Coverage(
                    of = b,
                    unitResults = setOf(
                        coveredLine(benchClass, 39, 3),
                        coveredLine(benchClass, 51, 14),
                        coveredLine(benchClass, 52, 1),
                        coveredLine(benchClass, 56, 3),
                        coveredLine(benchClass, 57, 1)
                    ) + coreALineCoverages + coreDLineCoverages
                )
            )
        }

        internal fun bench4LineCov(b: Benchmark): Pair<Benchmark, Coverage> = bench4LineCoverage(b)

        val bench4LineCovs: Array<Pair<Benchmark, Coverage>> = JarTestHelper.BenchParameterized2.bench4.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { bench4LineCov(it) }.toTypedArray()
        }

        val bench4LineNonParamCovs: Array<Pair<Benchmark, Coverage>> =
            arrayOf(bench4LineCov(JarTestHelper.BenchParameterized2.bench4))
    }

    object BenchParameterized2v2 {
        val bench4MethodCovs: Array<Pair<Benchmark, Coverage>> = JarTestHelper.BenchParameterized2v2.bench4.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { BenchParameterized2.bench4MethodCov(it) }.toTypedArray()
        }

        val bench4MethodNonParamCovs: Array<Pair<Benchmark, Coverage>> =
                arrayOf(BenchParameterized2.bench4MethodCov(JarTestHelper.BenchParameterized2v2.bench4))

        val bench4LineCovs: Array<Pair<Benchmark, Coverage>> = JarTestHelper.BenchParameterized2v2.bench4.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { BenchParameterized2.bench4LineCov(it) }.toTypedArray()
        }

        val bench4LineNonParamCovs: Array<Pair<Benchmark, Coverage>> =
            arrayOf(BenchParameterized2.bench4LineCov(JarTestHelper.BenchParameterized2v2.bench4))
    }

    object NestedBenchmark {

        object Bench1 {
            private val bench11MethodCoverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.Bench1.bench11.let { b ->
                val pb = b.toPlainMethod()
                Pair(
                    b,
                    Coverage(
                        of = b,
                        unitResults = setOf(
                            Covered(unit = JarTestHelper.NestedBenchmark.Bench1.constructor.toCoverageUnit(), level = -1),
                            Covered(unit = pb.toCoverageUnit(), level = -1)
                        )
                    )
                )
            }

            val bench11MethodCov: Pair<Benchmark, Coverage> = bench11MethodCoverage

            private val bench11LineCoverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.Bench1.let { benchClass ->
                val bench = benchClass.bench11
                Pair(
                    bench,
                    Coverage(
                        of = bench,
                        unitResults = setOf(
                            coveredLine(benchClass, 17, 3),
                            coveredLine(benchClass, 22, 3),
                            coveredLine(benchClass, 23, 1)
                        )
                    )
                )
            }

            val bench11LineCov: Pair<Benchmark, Coverage> = bench11LineCoverage

            private val bench12MethodCoverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.Bench1.bench12.let { b ->
                val pb = b.toPlainMethod()
                Pair(
                    b,
                    Coverage(
                        of = b,
                        unitResults = setOf(
                            Covered(unit = JarTestHelper.NestedBenchmark.Bench1.constructor.toCoverageUnit(), level = -1),
                            Covered(unit = pb.toCoverageUnit(), level = -1)
                        )
                    )
                )
            }

            val bench12MethodCov: Pair<Benchmark, Coverage> = bench12MethodCoverage

            private val bench12LineCoverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.Bench1.let { benchClass ->
                val bench = benchClass.bench12
                Pair(
                    bench,
                    Coverage(
                        of = bench,
                        unitResults = setOf(
                            coveredLine(benchClass, 17, 3),
                            coveredLine(benchClass, 27, 3),
                            coveredLine(benchClass, 28, 1)
                        )
                    )
                )
            }

            val bench12LineCov: Pair<Benchmark, Coverage> = bench12LineCoverage
        }

        private val bench2MethodCoverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.bench2.let { b ->
            val pb = b.toPlainMethod()
            Pair(
                b,
                Coverage(
                    of = b,
                    unitResults = setOf(
                        Covered(unit = JarTestHelper.NestedBenchmark.constructor.toCoverageUnit(), level = -1),
                        Covered(unit = pb.toCoverageUnit(), level = -1)
                    )
                )
            )
        }

        val bench2MethodCov: Pair<Benchmark, Coverage> = bench2MethodCoverage

        private val bench2LineCoverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.let { benchClass ->
            val bench = benchClass.bench2
            Pair(
                bench,
                Coverage(
                    of = bench,
                    unitResults = setOf(
                        coveredLine(benchClass, 12, 3),
                        coveredLine(benchClass, 36, 3),
                        coveredLine(benchClass, 37, 1)
                    )
                )
            )
        }

        val bench2LineCov: Pair<Benchmark, Coverage> = bench2LineCoverage

        object Bench3 {
            private val bench31MethodCoverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.Bench3.bench31.let { b ->
                val pb = b.toPlainMethod()
                Pair(
                    b,
                    Coverage(
                        of = b,
                        unitResults = setOf(
                            Covered(unit = JarTestHelper.NestedBenchmark.Bench3.constructor.toCoverageUnit(), level = -1),
                            Covered(unit = pb.toCoverageUnit(), level = -1)
                        )
                    )
                )
            }

            val bench31MethodCov: Pair<Benchmark, Coverage> = bench31MethodCoverage

            private val bench31LineCoverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.Bench3.let { benchClass ->
                val bench = benchClass.bench31
                Pair(
                    bench,
                    Coverage(
                        of = bench,
                        unitResults = setOf(
                            coveredLine(benchClass, 40, 3),
                            coveredLine(benchClass, 46, 3),
                            coveredLine(benchClass, 47, 1)
                        )
                    )
                )
            }

            val bench31LineCov: Pair<Benchmark, Coverage> = bench31LineCoverage

            object Bench32 {
                private val bench321MethodCoverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.Bench3.Bench32.bench321.let { b ->
                    val pb = b.toPlainMethod()
                    Pair(
                        b,
                        Coverage(
                            of = b,
                            unitResults = setOf(
                                Covered(unit = JarTestHelper.NestedBenchmark.Bench3.Bench32.constructor.toCoverageUnit(), level = -1),
                                Covered(unit = pb.toCoverageUnit(), level = -1)
                            )
                        )
                    )
                }
                val bench321MethodCov: Pair<Benchmark, Coverage> = bench321MethodCoverage

                private val bench321LineCoverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.Bench3.Bench32.let { benchClass ->
                    val bench = benchClass.bench321
                    Pair(
                        bench,
                        Coverage(
                            of = bench,
                            unitResults = setOf(
                                coveredLine(benchClass, 49, 3),
                                coveredLine(benchClass, 53, 3),
                                coveredLine(benchClass, 54, 1)
                            )
                        )
                    )
                }
                val bench321LineCov: Pair<Benchmark, Coverage> = bench321LineCoverage
            }
        }
    }

    private fun coveredLine(file: JarTestHelper.HasFile, number: Int, ci: Int): Covered =
        Covered(
            unit = CoverageUnitLine(
                line = Line(
                    file = file.file,
                    number = number
                ),
                missedInstructions = 0,
                coveredInstructions = ci,
                missedBranches = 0,
                coveredBranches = 0
            ),
            level = -1
        )

    val coreALineCoverages = setOf<CoverageUnitResult>(
        coveredLine(JarTestHelper.CoreA, 6, 3),
        coveredLine(JarTestHelper.CoreA, 8, 2),
        coveredLine(JarTestHelper.CoreA, 9, 3),
        coveredLine(JarTestHelper.CoreA, 10, 3),
        coveredLine(JarTestHelper.CoreA, 11, 1),
        coveredLine(JarTestHelper.CoreA, 15, 11),
        coveredLine(JarTestHelper.CoreA, 16, 3),
        coveredLine(JarTestHelper.CoreA, 17, 4),
        coveredLine(JarTestHelper.CoreA, 18, 1)
    )

    val coreBLineCoverages = setOf<CoverageUnitResult>(
        coveredLine(JarTestHelper.CoreB, 7, 2),
        coveredLine(JarTestHelper.CoreB, 8, 3),
        coveredLine(JarTestHelper.CoreB, 9, 3),
        coveredLine(JarTestHelper.CoreB, 10, 1),
        coveredLine(JarTestHelper.CoreB, 14, 11),
        coveredLine(JarTestHelper.CoreB, 15, 3),
        coveredLine(JarTestHelper.CoreB, 16, 1)
    )

    val coreCLineCoverages = setOf<CoverageUnitResult>(
        coveredLine(JarTestHelper.CoreC, 7, 2),
        coveredLine(JarTestHelper.CoreC, 8, 3),
        coveredLine(JarTestHelper.CoreC, 9, 1),
        coveredLine(JarTestHelper.CoreC, 12, 11),
        coveredLine(JarTestHelper.CoreC, 13, 3),
        coveredLine(JarTestHelper.CoreC, 14, 1)
    )

    val coreDLineCoverages = setOf<CoverageUnitResult>(
        coveredLine(JarTestHelper.CoreD, 3, 3),
        coveredLine(JarTestHelper.CoreD, 6, 3),
        coveredLine(JarTestHelper.CoreD, 7, 1)
    )

    private fun methodCoverages(bp1: Array<Pair<Benchmark, Coverage>>, bp2: Array<Pair<Benchmark, Coverage>>): Coverages =
        Coverages(mapOf(
            *bp1,
            BenchNonParameterized.bench2MethodCov,
            OtherBench.bench3MethodCov,
            *bp2,
            NestedBenchmark.Bench1.bench11MethodCov,
            NestedBenchmark.Bench1.bench12MethodCov,
            NestedBenchmark.bench2MethodCov,
            NestedBenchmark.Bench3.bench31MethodCov,
            NestedBenchmark.Bench3.Bench32.bench321MethodCov
        ))

    val methodCoverages = methodCoverages(BenchParameterized.bench1MethodCovs, BenchParameterized2.bench4MethodCovs)
    val methodCoveragesNonParam = methodCoverages(BenchParameterized.bench1MethodNonParamCovs, BenchParameterized2.bench4MethodNonParamCovs)

    val methodCoveragesV2 = methodCoverages(BenchParameterized.bench1MethodCovs, BenchParameterized2v2.bench4MethodCovs)
    val methodCoveragesV2NonParam = methodCoverages(BenchParameterized.bench1MethodNonParamCovs, BenchParameterized2v2.bench4MethodNonParamCovs)

    private fun lineCoverages(bp1: Array<Pair<Benchmark, Coverage>>, bp2: Array<Pair<Benchmark, Coverage>>): Coverages =
        Coverages(mapOf(
            *bp1,
            BenchNonParameterized.bench2LineCov,
            OtherBench.bench3LineCov,
            *bp2,
            NestedBenchmark.Bench1.bench11LineCov,
            NestedBenchmark.Bench1.bench12LineCov,
            NestedBenchmark.bench2LineCov,
            NestedBenchmark.Bench3.bench31LineCov,
            NestedBenchmark.Bench3.Bench32.bench321LineCov
        ))

    val lineCoverages = methodCoverages(BenchParameterized.bench1LineCovs, BenchParameterized2.bench4LineCovs)
    val lineCoveragesNonParam = methodCoverages(BenchParameterized.bench1LineNonParamCovs, BenchParameterized2.bench4LineNonParamCovs)

    val lineCoveragesV2 = lineCoverages(BenchParameterized.bench1LineCovs, BenchParameterized2v2.bench4LineCovs)
    val lineCoveragesNonParamV2 = lineCoverages(BenchParameterized.bench1LineNonParamCovs, BenchParameterized2v2.bench4LineNonParamCovs)

    val coverages = merge(methodCoverages, lineCoverages)
    val coveragesNonParam = merge(methodCoveragesNonParam, lineCoveragesNonParam)

    val coveragesV2 = merge(methodCoveragesV2, lineCoveragesV2)
    val coveragesNonParamV2 = merge(methodCoveragesV2NonParam, lineCoveragesNonParamV2)
}
