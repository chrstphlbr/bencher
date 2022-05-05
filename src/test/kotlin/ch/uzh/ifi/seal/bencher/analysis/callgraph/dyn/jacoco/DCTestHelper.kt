package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn.jacoco

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.Coverages
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.Coverage
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.Covered

object DCTestHelper {

    private fun emptyReachabilities(b: Benchmark): Pair<Benchmark, Coverage> = Pair(
            b,
            Coverage(of = b, unitResults = setOf())
    )

    object BenchParameterized {
        private fun bench1Reachabilities(b: Benchmark): Pair<Benchmark, Coverage> {
            val pb = b.toPlainMethod()
            return Pair(
                    b,
                    Coverage(
                            of = b,
                            unitResults = setOf(
                                    Covered(unit = JarTestHelper.BenchParameterized.constructor, level = -1),
                                    Covered(unit = JarTestHelper.BenchParameterized.setup.toPlainMethod(), level = -1),
                                    Covered(unit = pb, level = -1),
                                    Covered(unit = JarTestHelper.CoreA.constructor, level = -1),
                                    Covered(unit = JarTestHelper.CoreA.m, level = -1),
                                    Covered(unit = JarTestHelper.CoreB.constructor, level = -1),
                                    Covered(unit = JarTestHelper.CoreB.m, level = -1),
                                    Covered(unit = JarTestHelper.CoreC.constructor, level = -1),
                                    Covered(unit = JarTestHelper.CoreC.m, level = -1)
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
            val pb = b.toPlainMethod()
            Pair(
                    b,
                    Coverage(
                            of = b,
                            unitResults = setOf(
                                    Covered(unit = JarTestHelper.BenchNonParameterized.constructor, level = -1),
                                    Covered(unit = pb, level = -1),
                                    Covered(unit = JarTestHelper.CoreC.constructor, level = -1),
                                    Covered(unit = JarTestHelper.CoreC.m, level = -1)
                            )
                    )
            )
        }

        val bench2Cg: Pair<Benchmark, Coverage> = bench2Coverage
    }

    object OtherBench {
        private val bench3Coverage: Pair<Benchmark, Coverage> = JarTestHelper.OtherBench.bench3.let { b ->
            val pb = b.toPlainMethod()
            Pair(
                    b,
                    Coverage(
                            of = b,
                            unitResults = setOf(
                                    Covered(unit = JarTestHelper.OtherBench.constructor, level = -1),
                                    Covered(unit = JarTestHelper.OtherBench.setup.toPlainMethod(), level = -1),
                                    Covered(unit = pb, level = -1),
                                    Covered(unit = JarTestHelper.OtherBench.tearDown.toPlainMethod(), level = -1),
                                    Covered(unit = JarTestHelper.CoreB.constructor, level = -1),
                                    Covered(unit = JarTestHelper.CoreB.m, level = -1),
                                    Covered(unit = JarTestHelper.CoreC.constructor, level = -1),
                                    Covered(unit = JarTestHelper.CoreC.m, level = -1)
                            )
                    )
            )
        }

        val bench3Cg: Pair<Benchmark, Coverage> = bench3Coverage
    }

    object BenchParameterized2 {
        private fun bench4Reachabilities(b: Benchmark): Pair<Benchmark, Coverage> {
            val pb = b.toPlainMethod()
            return Pair(
                    b,
                    Coverage(
                            of = b,
                            unitResults = setOf(
                                    Covered(unit = JarTestHelper.BenchParameterized2.constructor, level = -1),
                                    Covered(unit = JarTestHelper.BenchParameterized2.setup.toPlainMethod(), level = -1),
                                    Covered(unit = pb, level = -1),
                                    Covered(unit = JarTestHelper.CoreA.constructor, level = -1),
                                    Covered(unit = JarTestHelper.CoreA.m, level = -1),
                                    Covered(unit = JarTestHelper.CoreD.constructor, level = -1),
                                    Covered(unit = JarTestHelper.CoreD.m, level = -1)
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
            private val bench11Coverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.Bench1.bench11.let { b ->
                val pb = b.toPlainMethod()
                Pair(
                        b,
                        Coverage(
                                of = b,
                                unitResults = setOf(
                                        Covered(unit = JarTestHelper.NestedBenchmark.Bench1.constructor, level = -1),
                                        Covered(unit = pb, level = -1)
                                )
                        )
                )
            }

            val bench11Cg: Pair<Benchmark, Coverage> = bench11Coverage

            private val bench12Coverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.Bench1.bench12.let { b ->
                val pb = b.toPlainMethod()
                Pair(
                        b,
                        Coverage(
                                of = b,
                                unitResults = setOf(
                                        Covered(unit = JarTestHelper.NestedBenchmark.Bench1.constructor, level = -1),
                                        Covered(unit = pb, level = -1)
                                )
                        )
                )
            }

            val bench12Cg: Pair<Benchmark, Coverage> = bench12Coverage
        }

        private val bench2Coverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.bench2.let { b ->
            val pb = b.toPlainMethod()
            Pair(
                    b,
                    Coverage(
                            of = b,
                            unitResults = setOf(
                                    Covered(unit = JarTestHelper.NestedBenchmark.constructor, level = -1),
                                    Covered(unit = pb, level = -1)
                            )
                    )
            )
        }

        val bench2Cg: Pair<Benchmark, Coverage> = bench2Coverage

        object Bench3 {
            private val bench31Coverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.Bench3.bench31.let { b ->
                val pb = b.toPlainMethod()
                Pair(
                        b,
                        Coverage(
                                of = b,
                                unitResults = setOf(
                                        Covered(unit = JarTestHelper.NestedBenchmark.Bench3.constructor, level = -1),
                                        Covered(unit = pb, level = -1)
                                )
                        )
                )
            }

            val bench31Cg: Pair<Benchmark, Coverage> = bench31Coverage

            object Bench32 {
                private val bench321Coverage: Pair<Benchmark, Coverage> = JarTestHelper.NestedBenchmark.Bench3.Bench32.bench321.let { b ->
                    val pb = b.toPlainMethod()
                    Pair(
                            b,
                            Coverage(
                                    of = b,
                                    unitResults = setOf(
                                            Covered(unit = JarTestHelper.NestedBenchmark.Bench3.Bench32.constructor, level = -1),
                                            Covered(unit = pb, level = -1)
                                    )
                            )
                    )
                }
                val bench321Cg: Pair<Benchmark, Coverage> = bench321Coverage
            }
        }
    }

    private fun cgResult(bp1: Array<Pair<Benchmark, Coverage>>, bp2: Array<Pair<Benchmark, Coverage>>): Coverages =
            Coverages(mapOf(
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
