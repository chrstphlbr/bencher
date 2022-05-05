package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn.javacallgraph

import arrow.core.getOrHandle
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.Covered
import ch.uzh.ifi.seal.bencher.analysis.finder.NoMethodFinderMock
import ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchFinder
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

        val cg = cge.get(jar.toPath()).getOrHandle {
            Assertions.fail<String>("Could not retrieve CG: $it")
            return
        }

        Assertions.assertEquals(0, cg.calls.size)
    }

    private fun checkCGResult(cgResult: CGResult, m: Method, ecs: List<Covered>) {
        val cs = cgResult.calls[m]
        if (cs == null) {
            Assertions.fail<String>("method $m has no calls")
            return
        }

        Assertions.assertEquals(m, cs.of)

        val s = cs.all().toList().size
        Assertions.assertEquals(ecs.size, s)

        ecs.forEach {
            val r = cs.single(m, it.unit)
            Assertions.assertEquals(it.copy(), r)
        }
    }

    @Test
    fun methodsCGperParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cge = JavaCallgraphDCG(
                benchmarkFinder = AsmBenchFinder(
                        jar = jar,
                        pkgPrefixes = setOf("org.sample")
                ),
                oneCGForParameterizedBenchmarks = false,
                inclusion = IncludeOnly(setOf("org.sample"))
        )

        val cg = cge.get(jar.toPath()).getOrHandle {
            Assertions.fail<String>("Could not retrieve CG: $it")
            return
        }
        Assertions.assertEquals(26, cg.calls.size)

        DCGTestHelper.cgResultv2.calls.forEach { m, rs ->
            checkCGResult(cg, m, rs.all().map { it as Covered })
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

        val cg = cge.get(jar.toPath()).getOrHandle {
            Assertions.fail<String>("Could not retrieve CG: $it")
            return
        }

        Assertions.assertEquals(0, cg.calls.size)
    }

    @Test
    fun methodsOneCGperParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cge = JavaCallgraphDCG(
                benchmarkFinder = AsmBenchFinder(
                        jar = jar,
                        pkgPrefixes = setOf("org.sample")
                ),
                oneCGForParameterizedBenchmarks = true,
                inclusion = IncludeOnly(setOf("org.sample"))
        )

        val cg = cge.get(jar.toPath()).getOrHandle {
            Assertions.fail<String>("Could not retrieve CG: $it")
            return
        }
        Assertions.assertEquals(13, cg.calls.size)

        DCGTestHelper.cgResultv2NonParam.calls.forEach { m, rs ->
            checkCGResult(cg, m, rs.all().map { it as Covered })
        }
    }
}
