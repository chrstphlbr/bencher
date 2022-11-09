package ch.uzh.ifi.seal.bencher.analysis.coverage.sta

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitMethod
import ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchFinder
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class WalaSCLibOnlyMultiCGTest : WalaSCTest() {

    override val covs: Coverages
        get() = WalaSCLibOnlyMultiCGTest.cov

    override val multiCGEntrypoints = true

    @Test
    fun libOnlyCalls() {
        val justLibCalls = cov.all().fold(true) { acc, mc ->
            val cond: Boolean = when (val u = mc.unit) {
                is CoverageUnitMethod -> u.method.clazz.startsWith(pkgPrefix)
                else -> true // do not care about other types of coverage
            }

            acc && cond
        }

        Assertions.assertTrue(justLibCalls, "Non-lib calls in Coverages")
    }

    companion object {
        val h = WalaSCTestHelper
        lateinit var cov: Coverages

        val pkgPrefix = "org.sample"

        @JvmStatic
        @BeforeAll
        fun setup() {
            val jar = JarTestHelper.jar4BenchsJmh121.fileResource()

            cov = h.assertCoverages(
                    WalaSC(
                            entrypoints = CGEntrypoints(
                                    mf = AsmBenchFinder(jar),
                                    me = BenchmarkWithSetupTearDownEntrypoints(),
                                    ea = MultiCGEntrypoints()
                            ),
                            algo = WalaRTA(),
                            inclusions = IncludeOnly(setOf(pkgPrefix))
                    ),
                    jar = jar
            )
        }
    }
}
