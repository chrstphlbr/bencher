package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.fileResource
import java.io.File

object CGTestHelper {
    val b1Cg = JarTestHelper.BenchParameterized.bench1.let { b ->
        val pb = b.toPlainMethod()
        Pair(
                b,
                CG(
                        start = b,
                        edges = setOf(
                                MethodCall(from = pb, to = JarTestHelper.CoreA.m, idPossibleTargets = 0, nrPossibleTargets = 2),
                                MethodCall(from = pb, to = JarTestHelper.CoreB.m, idPossibleTargets = 0, nrPossibleTargets = 2),
                                MethodCall(from = JarTestHelper.CoreA.m, to = JarTestHelper.CoreA.m, idPossibleTargets = 1, nrPossibleTargets = 2),
                                MethodCall(from = JarTestHelper.CoreA.m, to = JarTestHelper.CoreB.m, idPossibleTargets = 1, nrPossibleTargets = 2),
                                MethodCall(from = JarTestHelper.CoreB.m, to = JarTestHelper.CoreC.m, idPossibleTargets = 2, nrPossibleTargets = 1),
                                MethodCall(from = JarTestHelper.CoreC.m, to = JarTestHelper.CoreE.mn1_1, idPossibleTargets = 3, nrPossibleTargets = 2),
                                MethodCall(from = JarTestHelper.CoreC.m, to = JarTestHelper.CoreE.mn2, idPossibleTargets = 3, nrPossibleTargets = 2)
                        )
                )
        )
    }

    val b2Cg = JarTestHelper.BenchNonParameterized.bench2.let { b ->
        val pb = b.toPlainMethod()
        Pair(
                b,
                CG(
                        start = b,
                        edges = setOf(
                                MethodCall(from = pb, to = JarTestHelper.CoreC.m, idPossibleTargets = 0, nrPossibleTargets = 1)
                        )
                )
        )
    }

    val b3Cg = JarTestHelper.OtherBench.bench3.let { b ->
        val pb = b.toPlainMethod()
        Pair(
                b,
                CG(
                        start = b,
                        edges = setOf(
                                MethodCall(from = pb, to = JarTestHelper.CoreB.m, idPossibleTargets = 0, nrPossibleTargets = 1),
                                MethodCall(from = JarTestHelper.CoreB.m, to = JarTestHelper.CoreC.m, idPossibleTargets = 1, nrPossibleTargets = 1)
                        )
                )
        )
    }

    val b4Cg = JarTestHelper.BenchParameterized2.bench4.let { b ->
        val pb = b.toPlainMethod()
        Pair(
                b,
                CG(
                        start = b,
                        edges = setOf(
                                MethodCall(from = pb, to = JarTestHelper.CoreA.m, idPossibleTargets = 0, nrPossibleTargets = 2),
                                MethodCall(from = pb, to = JarTestHelper.CoreD.m, idPossibleTargets = 0, nrPossibleTargets = 2),
                                MethodCall(from = JarTestHelper.CoreA.m, to = JarTestHelper.CoreA.m, idPossibleTargets = 1, nrPossibleTargets = 2),
                                MethodCall(from = JarTestHelper.CoreA.m, to = JarTestHelper.CoreD.m, idPossibleTargets = 1, nrPossibleTargets = 2)
                        )
                )
        )
    }

    object PrinterReader {
        val b4 = Benchmark(
                clazz = "org.sample.Bench99",
                name = "bench99",
                params = listOf("org.openjdk.jmh.infra.Blackhole", "java.lang.String"),
                jmhParams = listOf(Pair("str", "1"), Pair("str", "2"))
        )
        val b4Cg = Pair(
                b4,
                CG(
                        start = b4,
                        edges = setOf(
                                MethodCall(
                                        from = b4.toPlainMethod(),
                                        to = PlainMethod(
                                                clazz = "org.sample.CoreZ",
                                                name = "m",
                                                params = listOf("java.lang.String", "int[][]")
                                        ),
                                        idPossibleTargets = 0,
                                        nrPossibleTargets = 1
                                )
                        )
                )
        )

        val cgResult = CGResult(mapOf(CGTestHelper.b1Cg, CGTestHelper.b2Cg, CGTestHelper.b3Cg, b4Cg))

        val cgOut: File = "cgOut.txt".fileResource()
    }
}
