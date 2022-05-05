package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.PossiblyCovered
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Reachabilities
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Covered
import ch.uzh.ifi.seal.bencher.fileResource
import java.io.File

object CGTestHelper {
//    private val b1CG = JarTestHelper.BenchParameterized.bench1.let { b ->
//        val pb = b.toPlainMethod()
//        Pair(
//                b,
//                CG(
//                        start = b,
//                        edges = setOf(
//                                MethodCall(from = pb, to = JarTestHelper.CoreA.m, idPossibleTargets = 0, nrPossibleTargets = 2),
//                                MethodCall(from = pb, to = JarTestHelper.CoreB.m, idPossibleTargets = 0, nrPossibleTargets = 2),
//                                MethodCall(from = JarTestHelper.CoreA.m, to = JarTestHelper.CoreA.m, idPossibleTargets = 1, nrPossibleTargets = 2),
//                                MethodCall(from = JarTestHelper.CoreA.m, to = JarTestHelper.CoreB.m, idPossibleTargets = 1, nrPossibleTargets = 2),
//                                MethodCall(from = JarTestHelper.CoreB.m, to = JarTestHelper.CoreC.m, idPossibleTargets = 2, nrPossibleTargets = 1),
//                                MethodCall(from = JarTestHelper.CoreC.m, to = JarTestHelper.CoreE.mn1_1, idPossibleTargets = 3, nrPossibleTargets = 2),
//                                MethodCall(from = JarTestHelper.CoreC.m, to = JarTestHelper.CoreE.mn2, idPossibleTargets = 3, nrPossibleTargets = 2)
//                        )
//                )
//        )
//    }

    val b1Reachabilities = JarTestHelper.BenchParameterized.bench1.let { b ->
        Pair(
                b,
                Reachabilities(
                        start = b,
                        reachabilities = setOf(
                                PossiblyCovered(unit = JarTestHelper.CoreA.m, probability = 0.5, level = 1),
                                PossiblyCovered(unit = JarTestHelper.CoreB.m, probability = 0.5, level = 1),
                                PossiblyCovered(unit = JarTestHelper.CoreC.m, probability = 0.5, level = 2),
                                PossiblyCovered(unit = JarTestHelper.CoreE.mn1_1, probability = 0.25, level = 3),
                                PossiblyCovered(unit = JarTestHelper.CoreE.mn2, probability = 0.25, level = 3)
                        )
                )
        )
    }

    val b1Cg = b1Reachabilities


//    private val b2CG = JarTestHelper.BenchNonParameterized.bench2.let { b ->
//        val pb = b.toPlainMethod()
//        Pair(
//                b,
//                CG(
//                        start = b,
//                        edges = setOf(
//                                MethodCall(from = pb, to = JarTestHelper.CoreC.m, idPossibleTargets = 0, nrPossibleTargets = 1)
//                        )
//                )
//        )
//    }

    private val b2Reachabilities = JarTestHelper.BenchNonParameterized.bench2.let { b ->
        Pair(
                b,
                Reachabilities(
                        start = b,
                        reachabilities = setOf(
                                Covered(unit = JarTestHelper.CoreC.m, level = 1)
                        )
                )
        )
    }

    val b2Cg = b2Reachabilities

//    private val b3CG = JarTestHelper.OtherBench.bench3.let { b ->
//        val pb = b.toPlainMethod()
//        Pair(
//                b,
//                CG(
//                        start = b,
//                        edges = setOf(
//                                MethodCall(from = pb, to = JarTestHelper.CoreB.m, idPossibleTargets = 0, nrPossibleTargets = 1),
//                                MethodCall(from = JarTestHelper.CoreB.m, to = JarTestHelper.CoreC.m, idPossibleTargets = 1, nrPossibleTargets = 1)
//                        )
//                )
//        )
//    }

    private val b3Reachabilities = JarTestHelper.OtherBench.bench3.let { b ->
        Pair(
                b,
                Reachabilities(
                        start = b,
                        reachabilities = setOf(
                                Covered(unit = JarTestHelper.CoreB.m, level = 1),
                                Covered(unit = JarTestHelper.CoreC.m, level = 2)
                        )
                )
        )
    }

    val b3Cg = b3Reachabilities

//    private val b4CG = JarTestHelper.BenchParameterized2.bench4.let { b ->
//        val pb = b.toPlainMethod()
//        Pair(
//                b,
//                CG(
//                        start = b,
//                        edges = setOf(
//                                MethodCall(from = pb, to = JarTestHelper.CoreA.m, idPossibleTargets = 0, nrPossibleTargets = 2),
//                                MethodCall(from = pb, to = JarTestHelper.CoreD.m, idPossibleTargets = 0, nrPossibleTargets = 2),
//                                MethodCall(from = JarTestHelper.CoreA.m, to = JarTestHelper.CoreA.m, idPossibleTargets = 1, nrPossibleTargets = 2),
//                                MethodCall(from = JarTestHelper.CoreA.m, to = JarTestHelper.CoreD.m, idPossibleTargets = 1, nrPossibleTargets = 2)
//                        )
//                )
//        )
//    }

    private val b4Reachabilities = JarTestHelper.BenchParameterized2.bench4.let { b ->
        Pair(
                b,
                Reachabilities(
                        start = b,
                        reachabilities = setOf(
                                PossiblyCovered(unit = JarTestHelper.CoreA.m, probability = 0.5, level = 1),
                                PossiblyCovered(unit = JarTestHelper.CoreD.m, probability = 0.5, level = 1)
                        )
                )
        )
    }

    val b4Cg = b4Reachabilities

    object PrinterReader {
        val b4 = Benchmark(
                clazz = "org.sample.Bench99",
                name = "bench99",
                params = listOf("org.openjdk.jmh.infra.Blackhole", "java.lang.String"),
                returnType = SourceCodeConstants.void,
                jmhParams = listOf(Pair("str", "1"), Pair("str", "2"))
        )

//        private val b4CG = Pair(
//                b4,
//                CG(
//                        start = b4,
//                        edges = setOf(
//                                MethodCall(
//                                        from = b4.toPlainMethod(),
//                                        to = PlainMethod(
//                                                clazz = "org.sample.CoreZ",
//                                                name = "m",
//                                                params = listOf("java.lang.String", "int[][]"),
//                                                returnType = SourceCodeConstants.void
//                                        ),
//                                        idPossibleTargets = 0,
//                                        nrPossibleTargets = 1
//                                )
//                        )
//                )
//        )

        val b4Reachabilities = Pair(
                b4,
                Reachabilities(
                        start = b4,
                        reachabilities = setOf(
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

        val b4Cg = b4Reachabilities

        val cgResult = CGResult(mapOf(b1Cg, b2Cg, b3Cg, b4Cg))

        private val walaCgOutCG: File = "walaCgOutCG.txt".fileResource()

        private val walaCgOutReachabilities: File = "walaCgOutReachabilities.txt".fileResource()

        val cgOut: File = walaCgOutReachabilities
    }
}
