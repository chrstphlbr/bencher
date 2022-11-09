package ch.uzh.ifi.seal.bencher.analysis.coverage.dyn.javacallgraph

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.Coverage
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.Covered
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.toCoverageUnit

object DCTestHelper {

    private fun emptyCoverage(b: Benchmark): Pair<Benchmark, Coverage> = Pair(
            b,
            Coverage(of = b, unitResults = setOf())
    )

    object BenchParameterized {
        private fun bench1Coverage(b: Benchmark): Pair<Benchmark, Coverage> {
            return Pair(
                    b,
                    Coverage(
                            of = b,
                            unitResults = setOf(
                                    Covered(unit = JarTestHelper.CoreA.m.toCoverageUnit(), level = 1),
                                    Covered(unit = JarTestHelper.CoreB.m.toCoverageUnit(), level = 3),
                                    Covered(unit = JarTestHelper.CoreC.m.toCoverageUnit(), level = 4)
                            )
                    )
            )
        }

        private fun bench1Cov(b: Benchmark): Pair<Benchmark, Coverage> = bench1Coverage(b)


        val bench1Cov: Array<Pair<Benchmark, Coverage>> = JarTestHelper.BenchParameterized.bench1.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { bench1Cov(it) }.toTypedArray()
        }

        val bench1NonParamCov: Array<Pair<Benchmark, Coverage>> =
                arrayOf(bench1Cov(JarTestHelper.BenchParameterized.bench1))
    }

    object BenchNonParameterized {
        private val bench2Coverage: Pair<Benchmark, Coverage> = JarTestHelper.BenchNonParameterized.bench2.let { b ->
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

        val bench2Cov: Pair<Benchmark, Coverage> = bench2Coverage
    }

    object OtherBench {
        private val bench3Coverage: Pair<Benchmark, Coverage> = JarTestHelper.OtherBench.bench3.let { b ->
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

        val bench3Cov: Pair<Benchmark, Coverage> = bench3Coverage
    }

    object BenchParameterized2 {
        private fun bench4Coverage(b: Benchmark): Pair<Benchmark, Coverage> {
            return Pair(
                    b,
                    Coverage(
                            of = b,
                            unitResults = setOf(
                                    Covered(unit = JarTestHelper.CoreA.m.toCoverageUnit(), level = 1),
                                    Covered(unit = JarTestHelper.CoreD.m.toCoverageUnit(), level = 3)
                            )
                    )
            )
        }

        internal fun bench4Cov(b: Benchmark): Pair<Benchmark, Coverage> = bench4Coverage(b)

        val bench4Cov: Array<Pair<Benchmark, Coverage>> = JarTestHelper.BenchParameterized2.bench4.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { bench4Cov(it) }.toTypedArray()
        }

        val bench4NonParamCov: Array<Pair<Benchmark, Coverage>> =
                arrayOf(bench4Cov(JarTestHelper.BenchParameterized2.bench4))
    }

    object BenchParameterized2v2 {
        val bench4Cov: Array<Pair<Benchmark, Coverage>> = JarTestHelper.BenchParameterized2v2.bench4.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { BenchParameterized2.bench4Cov(it) }.toTypedArray()
        }

        val bench4NonParamCov: Array<Pair<Benchmark, Coverage>> =
                arrayOf(BenchParameterized2.bench4Cov(JarTestHelper.BenchParameterized2v2.bench4))
    }

    object NestedBenchmark {

        object Bench1 {
            val bench11Cov: Pair<Benchmark, Coverage> =
                    emptyCoverage(JarTestHelper.NestedBenchmark.Bench1.bench11)

            val bench12Cov: Pair<Benchmark, Coverage> =
                    emptyCoverage(JarTestHelper.NestedBenchmark.Bench1.bench12)
        }

        val bench2Cov: Pair<Benchmark, Coverage> = emptyCoverage(JarTestHelper.NestedBenchmark.bench2)

        object Bench3 {
            val bench31Cov: Pair<Benchmark, Coverage> =
                    emptyCoverage(JarTestHelper.NestedBenchmark.Bench3.bench31)

            object Bench32 {
                val bench321Cov: Pair<Benchmark, Coverage> =
                        emptyCoverage(JarTestHelper.NestedBenchmark.Bench3.Bench32.bench321)
            }
        }
    }

    private fun coverages(bp1: Array<Pair<Benchmark, Coverage>>, bp2: Array<Pair<Benchmark, Coverage>>): Coverages =
            Coverages(mapOf(
                    *bp1,
                    BenchNonParameterized.bench2Cov,
                    OtherBench.bench3Cov,
                    *bp2,
                    NestedBenchmark.Bench1.bench11Cov,
                    NestedBenchmark.Bench1.bench12Cov,
                    NestedBenchmark.bench2Cov,
                    NestedBenchmark.Bench3.bench31Cov,
                    NestedBenchmark.Bench3.Bench32.bench321Cov
            ))

    val coverages = coverages(BenchParameterized.bench1Cov, BenchParameterized2.bench4Cov)
    val coveragesNonParam = coverages(BenchParameterized.bench1NonParamCov, BenchParameterized2.bench4NonParamCov)

    val coveragesV2 = coverages(BenchParameterized.bench1Cov, BenchParameterized2v2.bench4Cov)
    val coveragesV2NonParam = coverages(BenchParameterized.bench1NonParamCov, BenchParameterized2v2.bench4NonParamCov)
}
