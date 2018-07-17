package ch.uzh.ifi.seal.bencher.analysis.sta

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.PossibleMethod
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.MethodCall
import ch.uzh.ifi.seal.bencher.analysis.callgraph.SimplePrinter
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaRTA
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaSCG
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.shrike.bench.Bench
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

class WalaSCGTest {

    fun print() {
        val p = SimplePrinter(System.out)
        p.print(cg)
    }

    @Test
    fun allBenchs() {
        Assertions.assertTrue(cg.benchCalls.containsKey(bench1), "bench1 not present")
        Assertions.assertTrue(cg.benchCalls.containsKey(bench2), "bench2 not present")
        Assertions.assertTrue(cg.benchCalls.containsKey(bench3), "bench3 not present")
    }

    @Test
    fun bench1Calls() {
        val calls = cg.benchCalls.get(bench1)
        if (calls == null) {
            Assertions.fail<String>("bench1 not present")
            return
        }


        val a1 = calls.contains(possibleMethodCall(coreA, 1, 2, 0))
        Assertions.assertTrue(a1, errStr("A.m", 1))

        val b1 = calls.contains(possibleMethodCall(coreB, 1, 2, 0))
        Assertions.assertTrue(b1, errStr("B.m", 1))

        val a2 = calls.contains(possibleMethodCall(coreA, 2, 2, 5))
        Assertions.assertTrue(a2, errStr("A.m", 2))
        val b2 = calls.contains(possibleMethodCall(coreB, 2, 2, 5))
        Assertions.assertTrue(b2, errStr("B.m", 2))
        val c2 = calls.contains(plainMethodCall(coreC, 2))
        Assertions.assertTrue(c2, errStr("C.m", 2))
    }

    fun errStr(call: String, level: Int): String =
            "call to $call on level $level not found"

    @Test
    fun bench2Calls() {
        val calls = cg.benchCalls.get(bench2)
        if (calls == null) {
            Assertions.fail<String>("bench2 not present")
            return
        }


        val c1 = calls.contains(plainMethodCall(coreC, 1))
        Assertions.assertTrue(c1, errStr("C.m", 1))
    }

    @Test
    fun bench3Calls() {
        val calls = cg.benchCalls.get(bench3)
        if (calls == null) {
            Assertions.fail<String>("bench3 not present")
            return
        }


        val b1 = calls.contains(plainMethodCall(coreB, 1))
        Assertions.assertTrue(b1, errStr("B.m", 1))
    }

    companion object {
        private lateinit var jar: File
        private lateinit var jarPath: String

        private lateinit var cg: CGResult

        private val bench1 = Benchmark(clazz = "org.sample.BenchParameterized", name = "bench1", params = listOf(), jmhParams = listOf(Pair("str", "1"), Pair("str", "2"), Pair("str", "3")))
        private val bench2 = Benchmark(clazz = "org.sample.BenchNonParameterized", name ="bench2", params = listOf(), jmhParams = listOf())
        private val bench3 = Benchmark(clazz = "org.sample.OtherBench", name = "bench3", params = listOf(), jmhParams = listOf())

        private val coreA = PlainMethod(clazz = "org.sample.core.CoreA", name = "m", params = listOf())
        private val coreB = PlainMethod(clazz = "org.sample.core.CoreB", name = "m", params = listOf())
        private val coreC = PlainMethod(clazz = "org.sample.core.CoreC", name = "m", params = listOf())

        @BeforeAll
        @JvmStatic
        fun setup() {
            jar = File(this::class.java.classLoader.getResource("benchmarks_3_jmh121.jar").toURI())
            jarPath = jar.absolutePath


            val e = WalaSCG(jarPath, JarBenchFinder(jarPath), WalaRTA())
            val cgRes = e.get()
            if (cgRes.isLeft()) {
                Assertions.fail<String>("Could not get CG: ${cgRes.left().get()}")
            }

            cg = cgRes.right().get()
        }

        fun plainMethodCall(m: Method, level: Int): MethodCall =
                MethodCall(
                        method = PlainMethod(
                                clazz = m.clazz,
                                name = m.name,
                                params = m.params
                        ),
                        level = level
                )

        fun possibleMethodCall(m: Method, level: Int, nrPossibleTargets: Int, idPossibleTargets: Int): MethodCall =
                MethodCall(
                        method = PossibleMethod(
                                clazz = m.clazz,
                                name = m.name,
                                params = m.params,
                                idPossibleTargets = idPossibleTargets,
                                nrPossibleTargets = nrPossibleTargets
                        ),
                        level = level
                )
    }
}
