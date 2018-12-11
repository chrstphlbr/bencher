package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.finder.BenchFinderMock
import ch.uzh.ifi.seal.bencher.analysis.finder.NoMethodFinderMock
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.ipa.callgraph.AnalysisScope
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.util.config.AnalysisScopeReader
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AllAplicationEntrypointsTest {

    private lateinit var scope: AnalysisScope
    private lateinit var ch: ClassHierarchy

    @BeforeEach
    fun setUp() {
        scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jarFile.absolutePath.toString(), exclFile.fileResource())
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
                    cgm.method.clazz.startsWith(pkgPrefix)
                }
            }.fold(true) { acc, n -> acc && n }


    @Test
    fun noMethods() {
        val epg = AllApplicationEntrypoints(NoMethodFinderMock(), pkgPrefix)

        val eeps = epg.generate(scope, ch)
        if (eeps.isLeft()) {
            Assertions.fail<String>("Could not generate entrypoints: ${eeps.left().get()}")
        }
        val eps = eeps.right().get().toList()

        Assertions.assertEquals(1, eps.size)
        Assertions.assertTrue(onlyPackageMethods(eps))
        Assertions.assertFalse(hasStartingMehod(eps))
    }

    @Test
    fun benchmarksStartingMethods() {
        val epg = AllApplicationEntrypoints(BenchFinderMock(), pkgPrefix)

        val eeps = epg.generate(scope, ch)
        if (eeps.isLeft()) {
            Assertions.fail<String>("Could not generate entrypoints: ${eeps.left().get()}")
        }
        val eps = eeps.right().get().toList()

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
                        JarTestHelper.BenchParameterized2.bench4 -> true
                        else -> false
                    }
                } else {
                    true
                }
            }
        }.fold(true) { acc, n -> acc && n}

        Assertions.assertEquals(4, startMethods)
        Assertions.assertTrue(valid)
    }

    companion object {
        private const val exclFile = "wala_exclusions.txt"
        private const val pkgPrefix = "org.sample"
        private val jarFile = JarTestHelper.jar4BenchsJmh121v2.fileResource()
    }
}
