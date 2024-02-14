package ch.uzh.ifi.seal.bencher.analysis.coverage.dyn.javacallgraph

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.Covered
import ch.uzh.ifi.seal.bencher.analysis.finder.NoMethodFinderMock
import ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchFinder
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JavaCallgraphDCTest {

    private val javaSettings = JarTestHelper.javaSettings

    @Test
    fun noMethodsCovPerParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cove = JavaCallgraphDC(
            benchmarkFinder = NoMethodFinderMock(),
            javaSettings = javaSettings,
            oneCovForParameterizedBenchmarks = false,
            inclusion = IncludeOnly(setOf("org.sample")),
        )

        val cov = cove.get(jar.toPath()).getOrElse {
            Assertions.fail<String>("Could not retrieve coverage: $it")
            return
        }

        Assertions.assertEquals(0, cov.coverages.size)
    }

    private fun checkCoverages(coverages: Coverages, m: Method, ecs: List<Covered>) {
        val cs = coverages.coverages[m]
        if (cs == null) {
            Assertions.fail<String>("method $m has no coverage")
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
    fun methodsCovPerParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cove = JavaCallgraphDC(
            benchmarkFinder = AsmBenchFinder(
                jar = jar,
                pkgPrefixes = setOf("org.sample")
            ),
            javaSettings = javaSettings,
            oneCovForParameterizedBenchmarks = false,
            inclusion = IncludeOnly(setOf("org.sample")),
        )

        val cov = cove.get(jar.toPath()).getOrElse {
            Assertions.fail<String>("Could not retrieve coverage: $it")
            return
        }
        Assertions.assertEquals(26, cov.coverages.size)

        DCTestHelper.coveragesV2.coverages.forEach { (m, rs) ->
            checkCoverages(cov, m, rs.all().map { it as Covered })
        }
    }

    @Test
    fun noMethodsOneCovPerParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cove = JavaCallgraphDC(
            benchmarkFinder = NoMethodFinderMock(),
            javaSettings = javaSettings,
            oneCovForParameterizedBenchmarks = true,
            inclusion = IncludeOnly(setOf("org.sample")),
        )

        val cov = cove.get(jar.toPath()).getOrElse {
            Assertions.fail<String>("Could not retrieve coverage: $it")
            return
        }

        Assertions.assertEquals(0, cov.coverages.size)
    }

    @Test
    fun methodsOneCovPerParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cove = JavaCallgraphDC(
            benchmarkFinder = AsmBenchFinder(
                jar = jar,
                pkgPrefixes = setOf("org.sample")
            ),
            javaSettings = javaSettings,
            oneCovForParameterizedBenchmarks = true,
            inclusion = IncludeOnly(setOf("org.sample")),
        )

        val cov = cove.get(jar.toPath()).getOrElse {
            Assertions.fail<String>("Could not retrieve coverage: $it")
            return
        }
        Assertions.assertEquals(13, cov.coverages.size)

        DCTestHelper.coveragesV2NonParam.coverages.forEach { (m, rs) ->
            checkCoverages(cov, m, rs.all().map { it as Covered })
        }
    }
}
