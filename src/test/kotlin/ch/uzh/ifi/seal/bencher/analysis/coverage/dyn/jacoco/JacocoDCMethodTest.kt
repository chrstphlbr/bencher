package ch.uzh.ifi.seal.bencher.analysis.coverage.dyn.jacoco

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitType
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.Covered
import ch.uzh.ifi.seal.bencher.analysis.finder.NoMethodFinderMock
import ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchFinder
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JacocoDCMethodTest {

    private val javaSettings = JarTestHelper.javaSettings

    @Test
    fun noMethodsCovPerParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cove = JacocoDC(
            benchmarkFinder = NoMethodFinderMock(),
            javaSettings = javaSettings,
            oneCoverageForParameterizedBenchmarks = false,
            coverageUnitType = CoverageUnitType.METHOD,
            inclusion = IncludeOnly(setOf("org.sample")),
        )

        val cov = cove.get(jar.toPath()).getOrElse {
            Assertions.fail<String>("Could not retrieve coverages: $it")
            return
        }

        Assertions.assertEquals(0, cov.coverages.size)
    }

    @Test
    fun methodsCovPerParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cove = JacocoDC(
            benchmarkFinder = AsmBenchFinder(
                jar = jar,
                pkgPrefixes = setOf("org.sample")
            ),
            javaSettings = javaSettings,
            oneCoverageForParameterizedBenchmarks = false,
            coverageUnitType = CoverageUnitType.METHOD,
            inclusion = IncludeOnly(setOf("org.sample")),
        )

        val cov = cove.get(jar.toPath()).getOrElse {
            Assertions.fail<String>("Could not retrieve coverages: $it")
            return
        }
        Assertions.assertEquals(26, cov.coverages.size)

        DCTestHelper.methodCoveragesV2.coverages.forEach { (m, rs) ->
            DCTestHelper.checkCovResult(cov, m, rs.all().map { it as Covered })
        }
    }

    @Test
    fun noMethodsOneCovPerParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cove = JacocoDC(
            benchmarkFinder = NoMethodFinderMock(),
            javaSettings = javaSettings,
            oneCoverageForParameterizedBenchmarks = true,
            coverageUnitType = CoverageUnitType.METHOD,
            inclusion = IncludeOnly(setOf("org.sample")),
        )

        val cov = cove.get(jar.toPath()).getOrElse {
            Assertions.fail<String>("Could not retrieve CG: $it")
            return
        }

        Assertions.assertEquals(0, cov.coverages.size)
    }

    @Test
    fun methodsOneCovPerParamBench() {
        val jar = JarTestHelper.jar4BenchsJmh121v2.fileResource()

        val cove = JacocoDC(
            benchmarkFinder = AsmBenchFinder(
                jar = jar,
                pkgPrefixes = setOf("org.sample")
            ),
            javaSettings = javaSettings,
            oneCoverageForParameterizedBenchmarks = true,
            coverageUnitType = CoverageUnitType.METHOD,
            inclusion = IncludeOnly(setOf("org.sample")),
        )

        val cov = cove.get(jar.toPath()).getOrElse {
            Assertions.fail<String>("Could not retrieve CG: $it")
            return
        }
        Assertions.assertEquals(13, cov.coverages.size)

        DCTestHelper.methodCoveragesV2NonParam.coverages.forEach { (m, rs) ->
            DCTestHelper.checkCovResult(cov, m, rs.all().map { it as Covered })
        }
    }
}
