package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.JarHelper

object CGTestHelper {
    val b1Cg = Pair(
            JarHelper.BenchParameterized.bench1,
            listOf(
                    MethodCall(JarHelper.CoreA.m, 1),
                    MethodCall(JarHelper.CoreB.m, 1),
                    MethodCall(JarHelper.CoreC.m, 2),
                    MethodCall(JarHelper.CoreA.m, 2),
                    MethodCall(JarHelper.CoreB.m, 2)
            )
    )

    val b2Cg = Pair(
            JarHelper.BenchNonParameterized.bench2,
            listOf(
                    MethodCall(JarHelper.CoreC.m, 1)
            )
    )

    val b3Cg = Pair(
            JarHelper.OtherBench.bench3,
            listOf(
                    MethodCall(JarHelper.CoreB.m, 1),
                    MethodCall(JarHelper.CoreC.m, 2)
            )
    )

    object PrinterReader {
        val b4Cg = Pair(
                Benchmark(
                        clazz = "org.sample.Bench99",
                        name = "bench99",
                        params = listOf("org.openjdk.jmh.infra.Blackhole", "java.lang.String"),
                        jmhParams = listOf(Pair("str", "1"), Pair("str", "2"))
                ),
                listOf(
                        MethodCall(
                                level = 1,
                                method = PlainMethod(
                                        clazz = "org.sample.CoreZ",
                                        name = "m",
                                        params = listOf("java.lang.String", "int[][]")
                                )
                        )
                )
        )

        val cgResult = CGResult(mapOf(CGTestHelper.b1Cg, CGTestHelper.b2Cg, CGTestHelper.b3Cg, b4Cg))

        val defaultIndent = "    "

        fun expectedOut(indent: String = defaultIndent): String  =
                    "Benchmark:\n" +
                            "Benchmark(clazz=org.sample.BenchParameterized, name=bench1, params=[], jmhParams=[(str, 1), (str, 2), (str, 3)])\n" +
                            indent +"PlainMethod(clazz=org.sample.core.CoreA, name=m, params=[])\n" +
                            indent + "PlainMethod(clazz=org.sample.core.CoreB, name=m, params=[])\n" +
                            indent.repeat(2) + "PlainMethod(clazz=org.sample.core.CoreC, name=m, params=[])\n" +
                            indent.repeat(2) + "PlainMethod(clazz=org.sample.core.CoreA, name=m, params=[])\n" +
                            indent.repeat(2) + "PlainMethod(clazz=org.sample.core.CoreB, name=m, params=[])\n" +
                            "Benchmark:\n" +
                            "Benchmark(clazz=org.sample.BenchNonParameterized, name=bench2, params=[], jmhParams=[])\n" +
                            indent + "PlainMethod(clazz=org.sample.core.CoreC, name=m, params=[])\n" +
                            "Benchmark:\n" +
                            "Benchmark(clazz=org.sample.OtherBench, name=bench3, params=[], jmhParams=[])\n" +
                            indent + "PlainMethod(clazz=org.sample.core.CoreB, name=m, params=[])\n" +
                            indent.repeat(2) + "PlainMethod(clazz=org.sample.core.CoreC, name=m, params=[])\n" +
                            "Benchmark:\n" +
                            "Benchmark(clazz=org.sample.Bench99, name=bench99, params=[org.openjdk.jmh.infra.Blackhole, java.lang.String], jmhParams=[(str, 1), (str, 2)])\n" +
                            indent + "PlainMethod(clazz=org.sample.CoreZ, name=m, params=[java.lang.String, int[][]])\n"
    }
}
