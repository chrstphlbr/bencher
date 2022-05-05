package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn.jacoco

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

class JacocoDCTest {

    @Test
    fun noMethodsCovperParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cge = JacocoDC(
                benchmarkFinder = NoMethodFinderMock(),
                oneCoverageForParameterizedBenchmarks = false,
                inclusion = IncludeOnly(setOf("org.sample"))
        )

        val cg = cge.get(jar.toPath()).getOrHandle {
            Assertions.fail<String>("Could not retrieve CG: $it")
            return
        }

        Assertions.assertEquals(0, cg.calls.size)
    }

    private fun checkCovResult(cgResult: CGResult, m: Method, ecs: List<Covered>) {
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
    fun methodsCovperParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cge = JacocoDC(
                benchmarkFinder = AsmBenchFinder(
                        jar = jar,
                        pkgPrefixes = setOf("org.sample")
                ),
                oneCoverageForParameterizedBenchmarks = false,
                inclusion = IncludeOnly(setOf("org.sample"))
        )

        val cg = cge.get(jar.toPath()).getOrHandle {
            Assertions.fail<String>("Could not retrieve CG: $it")
            return
        }
        Assertions.assertEquals(26, cg.calls.size)

        DCTestHelper.cgResultv2.calls.forEach { m, rs ->
            checkCovResult(cg, m, rs.all().map { it as Covered })
        }
    }

    @Test
    fun noMethodsOneCovperParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cge = JacocoDC(
                benchmarkFinder = NoMethodFinderMock(),
                oneCoverageForParameterizedBenchmarks = true,
                inclusion = IncludeOnly(setOf("org.sample"))
        )

        val cg = cge.get(jar.toPath()).getOrHandle {
            Assertions.fail<String>("Could not retrieve CG: $it")
            return
        }

        Assertions.assertEquals(0, cg.calls.size)
    }

    @Test
    fun methodsOneCovperParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cge = JacocoDC(
                benchmarkFinder = AsmBenchFinder(
                        jar = jar,
                        pkgPrefixes = setOf("org.sample")
                ),
                oneCoverageForParameterizedBenchmarks = true,
                inclusion = IncludeOnly(setOf("org.sample"))
        )

        val cg = cge.get(jar.toPath()).getOrHandle {
            Assertions.fail<String>("Could not retrieve CG: $it")
            return
        }
        Assertions.assertEquals(13, cg.calls.size)

        DCTestHelper.cgResultv2NonParam.calls.forEach { m, rs ->
            checkCovResult(cg, m, rs.all().map { it as Covered })
        }
    }
}
