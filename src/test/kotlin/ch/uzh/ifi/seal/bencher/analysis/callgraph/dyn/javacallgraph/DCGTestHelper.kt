package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn.javacallgraph

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.Coverage
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.Covered

object DCGTestHelper {

    private fun emptyReachabilities(b: Benchmark): Pair<Benchmark, Coverage> = Pair(
            b,
            Coverage(of = b, unitResults = setOf())
    )

    object BenchParameterized {
        private fun bench1Reachabilities(b: Benchmark): Pair<Benchmark, Coverage> {
            return Pair(
                    b,
                    Coverage(
                            of = b,
                            unitResults = setOf(
                                    Covered(unit = JarTestHelper.CoreA.m, level = 1),
                                    Covered(unit = JarTestHelper.CoreB.m, level = 3),
                                    Covered(unit = JarTestHelper.CoreC.m, level = 4)
                            )
                    )
            )
        }

        private fun bench1Cg(b: Benchmark): Pair<Benchmark, Coverage> = bench1Reachabilities(b)


        val bench1Cgs: Array<Pair<Benchmark, Coverage>> = JarTestHelper.BenchParameterized.bench1.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { bench1Cg(it) }.toTypedArray()
        }

        val bench1NonParamCgs: Array<Pair<Benchmark, Coverage>> =
                arrayOf(bench1Cg(JarTestHelper.BenchParameterized.bench1))
    }

    object BenchNonParameterized {
        private val bench2Coverage: Pair<Benchmark, Coverage> = JarTestHelper.BenchNonParameterized.bench2.let { b ->
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

        val bench2Cg: Pair<Benchmark, Coverage> = bench2Coverage
    }

    object OtherBench {
        private val bench3Coverage: Pair<Benchmark, Coverage> = JarTestHelper.OtherBench.bench3.let { b ->
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

        val bench3Cg: Pair<Benchmark, Coverage> = bench3Coverage
    }

    object BenchParameterized2 {
        private fun bench4Reachabilities(b: Benchmark): Pair<Benchmark, Coverage> {
            return Pair(
                    b,
                    Coverage(
                            of = b,
                            unitResults = setOf(
                                    Covered(unit = JarTestHelper.CoreA.m, level = 1),
                                    Covered(unit = JarTestHelper.CoreD.m, level = 3)
                            )
                    )
            )
        }

        internal fun bench4Cg(b: Benchmark): Pair<Benchmark, Coverage> = bench4Reachabilities(b)

        val bench4Cgs: Array<Pair<Benchmark, Coverage>> = JarTestHelper.BenchParameterized2.bench4.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { bench4Cg(it) }.toTypedArray()
        }

        val bench4NonParamCgs: Array<Pair<Benchmark, Coverage>> =
                arrayOf(bench4Cg(JarTestHelper.BenchParameterized2.bench4))
    }

    object BenchParameterized2v2 {
        val bench4Cgs: Array<Pair<Benchmark, Coverage>> = JarTestHelper.BenchParameterized2v2.bench4.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { BenchParameterized2.bench4Cg(it) }.toTypedArray()
        }

        val bench4NonParamCgs: Array<Pair<Benchmark, Coverage>> =
                arrayOf(BenchParameterized2.bench4Cg(JarTestHelper.BenchParameterized2v2.bench4))
    }

    object NestedBenchmark {

        object Bench1 {
            val bench11Cg: Pair<Benchmark, Coverage> =
                    emptyReachabilities(JarTestHelper.NestedBenchmark.Bench1.bench11)

            val bench12Cg: Pair<Benchmark, Coverage> =
                    emptyReachabilities(JarTestHelper.NestedBenchmark.Bench1.bench12)
        }

        val bench2Cg: Pair<Benchmark, Coverage> = emptyReachabilities(JarTestHelper.NestedBenchmark.bench2)

        object Bench3 {
            val bench31Cg: Pair<Benchmark, Coverage> =
                    emptyReachabilities(JarTestHelper.NestedBenchmark.Bench3.bench31)

            object Bench32 {
                val bench321Cg: Pair<Benchmark, Coverage> =
                        emptyReachabilities(JarTestHelper.NestedBenchmark.Bench3.Bench32.bench321)
            }
        }
    }

    private fun cgResult(bp1: Array<Pair<Benchmark, Coverage>>, bp2: Array<Pair<Benchmark, Coverage>>): CGResult =
            CGResult(mapOf(
                    *bp1,
                    BenchNonParameterized.bench2Cg,
                    OtherBench.bench3Cg,
                    *bp2,
                    NestedBenchmark.Bench1.bench11Cg,
                    NestedBenchmark.Bench1.bench12Cg,
                    NestedBenchmark.bench2Cg,
                    NestedBenchmark.Bench3.bench31Cg,
                    NestedBenchmark.Bench3.Bench32.bench321Cg
            ))

    val cgResult = cgResult(BenchParameterized.bench1Cgs, BenchParameterized2.bench4Cgs)
    val cgResultNonParam = cgResult(BenchParameterized.bench1NonParamCgs, BenchParameterized2.bench4NonParamCgs)

    val cgResultv2 = cgResult(BenchParameterized.bench1Cgs, BenchParameterized2v2.bench4Cgs)
    val cgResultv2NonParam = cgResult(BenchParameterized.bench1NonParamCgs, BenchParameterized2v2.bench4NonParamCgs)
}
