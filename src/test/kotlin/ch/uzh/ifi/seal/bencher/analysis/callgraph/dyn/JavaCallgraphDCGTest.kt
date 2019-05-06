package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.callgraph.Reachable
import ch.uzh.ifi.seal.bencher.analysis.finder.AsmBenchFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.NoMethodFinderMock
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JavaCallgraphDCGTest {

    @Test
    fun noMethodsCGperParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cge = JavaCallgraphDCG(
                benchmarkFinder = NoMethodFinderMock(),
                oneCGForParameterizedBenchmarks = false,
                inclusion = IncludeOnly(setOf("org.sample"))
        )

        val ecg = cge.get(jar.toPath())
        if (ecg.isLeft()) {
            Assertions.fail<String>("Could not retrieve CG: ${ecg.left().get()}")
            return
        }
        val cg = ecg.right().get()

        Assertions.assertEquals(0, cg.calls.size)
    }

    private fun checkCGResult(cgResult: CGResult, m: Method, ecs: List<Reachable>) {
        val cs = cgResult.calls[m]
        if (cs == null) {
            Assertions.fail<String>("method $m has no calls")
            return
        }

        Assertions.assertEquals(m, cs.start)

        val s = cs.toList().size
        Assertions.assertEquals(ecs.size, s)

        ecs.forEach {
            val r = cs.reachable(m, it.to)
            Assertions.assertEquals(it.copy(from = m), r)
        }
    }

    @Test
    fun methodsCGperParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cge = JavaCallgraphDCG(
                benchmarkFinder = AsmBenchFinder(
                        jar = jar,
                        pkgPrefix = "org.sample"
                ),
                oneCGForParameterizedBenchmarks = false,
                inclusion = IncludeOnly(setOf("org.sample"))
        )

        val ecg = cge.get(jar.toPath())
        if (ecg.isLeft()) {
            Assertions.fail<String>("Could not retrieve CG: ${ecg.left().get()}")
            return
        }
        val cg = ecg.right().get()
        Assertions.assertEquals(19, cg.calls.size)

        DCGTestHelper.cgResultv2.calls.forEach { m, rs ->
            checkCGResult(cg, m, rs.toList().map { it as Reachable })
        }
    }

    @Test
    fun noMethodsOneCGperParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cge = JavaCallgraphDCG(
                benchmarkFinder = NoMethodFinderMock(),
                oneCGForParameterizedBenchmarks = true,
                inclusion = IncludeOnly(setOf("org.sample"))
        )

        val ecg = cge.get(jar.toPath())
        if (ecg.isLeft()) {
            Assertions.fail<String>("Could not retrieve CG: ${ecg.left().get()}")
            return
        }
        val cg = ecg.right().get()

        Assertions.assertEquals(0, cg.calls.size)
    }

    @Test
    fun methodsOneCGperParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cge = JavaCallgraphDCG(
                benchmarkFinder = AsmBenchFinder(
                        jar = jar,
                        pkgPrefix = "org.sample"
                ),
                oneCGForParameterizedBenchmarks = true,
                inclusion = IncludeOnly(setOf("org.sample"))
        )

        val ecg = cge.get(jar.toPath())
        if (ecg.isLeft()) {
            Assertions.fail<String>("Could not retrieve CG: ${ecg.left().get()}")
            return
        }
        val cg = ecg.right().get()
        Assertions.assertEquals(9, cg.calls.size)

        DCGTestHelper.cgResultv2NonParam.calls.forEach { m, rs ->
            checkCGResult(cg, m, rs.toList().map { it as Reachable })
        }
    }
}
