package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn.jacoco

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Reachabilities
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Covered

object DCTestHelper {

    private fun emptyReachabilities(b: Benchmark): Pair<Benchmark, Reachabilities> = Pair(
            b,
            Reachabilities(start = b, reachabilities = setOf())
    )

    object BenchParameterized {
        private fun bench1Reachabilities(b: Benchmark): Pair<Benchmark, Reachabilities> {
            val pb = b.toPlainMethod()
            return Pair(
                    b,
                    Reachabilities(
                            start = b,
                            reachabilities = setOf(
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

        private fun bench1Cg(b: Benchmark): Pair<Benchmark, Reachabilities> = bench1Reachabilities(b)


        val bench1Cgs: Array<Pair<Benchmark, Reachabilities>> = JarTestHelper.BenchParameterized.bench1.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { bench1Cg(it) }.toTypedArray()
        }

        val bench1NonParamCgs: Array<Pair<Benchmark, Reachabilities>> =
                arrayOf(bench1Cg(JarTestHelper.BenchParameterized.bench1))
    }

    object BenchNonParameterized {
        private val bench2Reachabilities: Pair<Benchmark, Reachabilities> = JarTestHelper.BenchNonParameterized.bench2.let { b ->
            val pb = b.toPlainMethod()
            Pair(
                    b,
                    Reachabilities(
                            start = b,
                            reachabilities = setOf(
                                    Covered(unit = JarTestHelper.BenchNonParameterized.constructor, level = -1),
                                    Covered(unit = pb, level = -1),
                                    Covered(unit = JarTestHelper.CoreC.constructor, level = -1),
                                    Covered(unit = JarTestHelper.CoreC.m, level = -1)
                            )
                    )
            )
        }

        val bench2Cg: Pair<Benchmark, Reachabilities> = bench2Reachabilities
    }

    object OtherBench {
        private val bench3Reachabilities: Pair<Benchmark, Reachabilities> = JarTestHelper.OtherBench.bench3.let { b ->
            val pb = b.toPlainMethod()
            Pair(
                    b,
                    Reachabilities(
                            start = b,
                            reachabilities = setOf(
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

        val bench3Cg: Pair<Benchmark, Reachabilities> = bench3Reachabilities
    }

    object BenchParameterized2 {
        private fun bench4Reachabilities(b: Benchmark): Pair<Benchmark, Reachabilities> {
            val pb = b.toPlainMethod()
            return Pair(
                    b,
                    Reachabilities(
                            start = b,
                            reachabilities = setOf(
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

        internal fun bench4Cg(b: Benchmark): Pair<Benchmark, Reachabilities> = bench4Reachabilities(b)

        val bench4Cgs: Array<Pair<Benchmark, Reachabilities>> = JarTestHelper.BenchParameterized2.bench4.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { bench4Cg(it) }.toTypedArray()
        }

        val bench4NonParamCgs: Array<Pair<Benchmark, Reachabilities>> =
                arrayOf(bench4Cg(JarTestHelper.BenchParameterized2.bench4))
    }

    object BenchParameterized2v2 {
        val bench4Cgs: Array<Pair<Benchmark, Reachabilities>> = JarTestHelper.BenchParameterized2v2.bench4.let { b ->
            val pbs = b.parameterizedBenchmarks()
            pbs.map { BenchParameterized2.bench4Cg(it) }.toTypedArray()
        }

        val bench4NonParamCgs: Array<Pair<Benchmark, Reachabilities>> =
                arrayOf(BenchParameterized2.bench4Cg(JarTestHelper.BenchParameterized2v2.bench4))
    }

    object NestedBenchmark {

        object Bench1 {
            private val bench11Reachabilities: Pair<Benchmark, Reachabilities> = JarTestHelper.NestedBenchmark.Bench1.bench11.let { b ->
                val pb = b.toPlainMethod()
                Pair(
                        b,
                        Reachabilities(
                                start = b,
                                reachabilities = setOf(
                                        Covered(unit = JarTestHelper.NestedBenchmark.Bench1.constructor, level = -1),
                                        Covered(unit = pb, level = -1)
                                )
                        )
                )
            }

            val bench11Cg: Pair<Benchmark, Reachabilities> = bench11Reachabilities

            private val bench12Reachabilities: Pair<Benchmark, Reachabilities> = JarTestHelper.NestedBenchmark.Bench1.bench12.let { b ->
                val pb = b.toPlainMethod()
                Pair(
                        b,
                        Reachabilities(
                                start = b,
                                reachabilities = setOf(
                                        Covered(unit = JarTestHelper.NestedBenchmark.Bench1.constructor, level = -1),
                                        Covered(unit = pb, level = -1)
                                )
                        )
                )
            }

            val bench12Cg: Pair<Benchmark, Reachabilities> = bench12Reachabilities
        }

        private val bench2Reachabilities: Pair<Benchmark, Reachabilities> = JarTestHelper.NestedBenchmark.bench2.let { b ->
            val pb = b.toPlainMethod()
            Pair(
                    b,
                    Reachabilities(
                            start = b,
                            reachabilities = setOf(
                                    Covered(unit = JarTestHelper.NestedBenchmark.constructor, level = -1),
                                    Covered(unit = pb, level = -1)
                            )
                    )
            )
        }

        val bench2Cg: Pair<Benchmark, Reachabilities> = bench2Reachabilities

        object Bench3 {
            private val bench31Reachabilities: Pair<Benchmark, Reachabilities> = JarTestHelper.NestedBenchmark.Bench3.bench31.let { b ->
                val pb = b.toPlainMethod()
                Pair(
                        b,
                        Reachabilities(
                                start = b,
                                reachabilities = setOf(
                                        Covered(unit = JarTestHelper.NestedBenchmark.Bench3.constructor, level = -1),
                                        Covered(unit = pb, level = -1)
                                )
                        )
                )
            }

            val bench31Cg: Pair<Benchmark, Reachabilities> = bench31Reachabilities

            object Bench32 {
                private val bench321Reachabilities: Pair<Benchmark, Reachabilities> = JarTestHelper.NestedBenchmark.Bench3.Bench32.bench321.let { b ->
                    val pb = b.toPlainMethod()
                    Pair(
                            b,
                            Reachabilities(
                                    start = b,
                                    reachabilities = setOf(
                                            Covered(unit = JarTestHelper.NestedBenchmark.Bench3.Bench32.constructor, level = -1),
                                            Covered(unit = pb, level = -1)
                                    )
                            )
                    )
                }
                val bench321Cg: Pair<Benchmark, Reachabilities> = bench321Reachabilities
            }
        }
    }

    private fun cgResult(bp1: Array<Pair<Benchmark, Reachabilities>>, bp2: Array<Pair<Benchmark, Reachabilities>>): CGResult =
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
