package ch.uzh.ifi.seal.bencher.analysis.sta

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.PossibleMethod
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.MethodCall
import ch.uzh.ifi.seal.bencher.analysis.callgraph.SimplePrinter
import ch.uzh.ifi.seal.bencher.analysis.callgraph.WalaCGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaSCG
import org.junit.jupiter.api.Assertions

object WalaSCGTestHelper {
    val bench1 = Benchmark(clazz = "org.sample.BenchParameterized", name = "bench1", params = listOf(), jmhParams = listOf(Pair("str", "1"), Pair("str", "2"), Pair("str", "3")))
    val bench2 = Benchmark(clazz = "org.sample.BenchNonParameterized", name ="bench2", params = listOf(), jmhParams = listOf())
    val bench3 = Benchmark(clazz = "org.sample.OtherBench", name = "bench3", params = listOf(), jmhParams = listOf())

    val coreA = PlainMethod(clazz = "org.sample.core.CoreA", name = "m", params = listOf())
    val coreB = PlainMethod(clazz = "org.sample.core.CoreB", name = "m", params = listOf())
    val coreC = PlainMethod(clazz = "org.sample.core.CoreC", name = "m", params = listOf())

    fun reachable(cg: CGResult, from: Benchmark, to: Method, level: Int) = reachable(cg, from, MethodCall(to, level))

    fun reachable(cg: CGResult, from: Benchmark, to: MethodCall) {
        val benchCalls = cg.benchCalls.get(from)
        if (benchCalls == null) {
            Assertions.fail<String>("No benchmark for $from")
            return
        }
        val call = benchCalls.find { it == to}
        Assertions.assertNotNull(call, "No method call ($to) from bench ($from) reachable")
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

    fun assertCGResult(wcg: WalaSCG): WalaCGResult {
        val cgRes = wcg.get()
        if (cgRes.isLeft()) {
            Assertions.fail<String>("Could not get CG: ${cgRes.left().get()}")
        }

        return cgRes.right().get()
    }

    fun errStr(call: String, level: Int): String =
            "call to $call on level $level not found"

    fun print(cg: CGResult) {
        val p = SimplePrinter(System.out)
        p.print(cg)
    }
}
