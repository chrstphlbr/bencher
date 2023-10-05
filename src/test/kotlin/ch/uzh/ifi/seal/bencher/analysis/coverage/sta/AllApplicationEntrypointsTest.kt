package ch.uzh.ifi.seal.bencher.analysis.coverage.sta

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.finder.BenchFinderMock
import ch.uzh.ifi.seal.bencher.analysis.finder.NoMethodFinderMock
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.core.util.config.AnalysisScopeReader
import com.ibm.wala.ipa.callgraph.AnalysisScope
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AllApplicationEntrypointsTest {

    private lateinit var scope: AnalysisScope
    private lateinit var ch: ClassHierarchy

    @BeforeEach
    fun setUp() {
        scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(jarFile.absolutePath.toString(), exclFile.fileResource())
        ch = ClassHierarchyFactory.make(scope)
    }

    private fun hasStartingMehod(eps: Entrypoints): Boolean =
            eps.flatMap {
                it.map { (cgm, _) ->
                    when (cgm) {
                        is CGAdditionalMethod -> false
                        is CGStartMethod -> true
                    }
                }
            }.fold(false) { acc, n -> acc || n }

    private fun onlyPackageMethods(eps: Entrypoints): Boolean =
            eps.flatMap {
                it.map { (cgm, _) ->
                    pkgPrefixes.fold(false) { acc, pkgPrefix ->
                        acc || cgm.method.clazz.startsWith(pkgPrefix)
                    }
                }
            }.fold(true) { acc, n -> acc && n }


    @Test
    fun noMethods() {
        val epg = AllApplicationEntrypoints(NoMethodFinderMock<Method>(), pkgPrefixes)

        val eps = epg.generate(scope, ch).getOrElse {
            Assertions.fail<String>("Could not generate entrypoints: $it")
            return
        }.toList()

        Assertions.assertEquals(1, eps.size)
        Assertions.assertTrue(onlyPackageMethods(eps))
        Assertions.assertFalse(hasStartingMehod(eps))
    }

    @Test
    fun benchmarksStartingMethods() {
        val epg = AllApplicationEntrypoints(BenchFinderMock(), pkgPrefixes)

        val eps = epg.generate(scope, ch).getOrElse {
            Assertions.fail<String>("Could not generate entrypoints: $it")
            return
        }.toList()

        Assertions.assertEquals(1, eps.size)
        Assertions.assertTrue(onlyPackageMethods(eps))

        var startMethods = 0

        val valid = eps.flatMap {
            it.map { (cgm, _) ->
                if (cgm is CGStartMethod) {
                    startMethods++
                    when (cgm.method) {
                        JarTestHelper.BenchParameterized.bench1 -> true
                        JarTestHelper.BenchNonParameterized.bench2 -> true
                        JarTestHelper.OtherBench.bench3 -> true
                        JarTestHelper.BenchParameterized2v2.bench4 -> true
                        else -> false
                    }
                } else {
                    true
                }
            }
        }.fold(true) { acc, n -> acc && n }

        Assertions.assertEquals(4, startMethods)
        Assertions.assertTrue(valid)
    }

    companion object {
        private const val exclFile = "wala_exclusions.txt"
        private val pkgPrefixes = setOf("org.sample", "org.sam")
        private val jarFile = JarTestHelper.jar4BenchsJmh121v2.fileResource()
    }
}
