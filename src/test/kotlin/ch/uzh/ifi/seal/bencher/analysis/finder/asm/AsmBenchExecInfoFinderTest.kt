package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AsmBenchExecInfoFinderTest : AbstractAsmBenchExecInfoTest() {

    @Test
    fun test() {
        val f = AsmBenchFinder(
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource(),
            pkgPrefixes = pkgPrefixes
        )

        val classExecInfos = f.classExecutionInfos().getOrElse {
            Assertions.fail<String>("Could not load class execution infos: $it")
            return
        }

        assertClassConfigs(classExecInfos)

        val benchExecInfos = f.benchmarkExecutionInfos().getOrElse {
            Assertions.fail<String>("Could not load benchmark execution infos: $it")
            return
        }

        assertBenchConfigs(benchExecInfos)
    }

    @Test
    fun testGroup() {
        val f = AsmBenchFinder(
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource(),
            pkgPrefixes = pkgPrefixes
        )

        val benchs = f.all().getOrElse {
            Assertions.fail<String>("Could not load benchmarks: $it")
            return
        }

        val b1 = benchs.filter { it == JarTestHelper.BenchsWithGroup.bench1 }.firstOrNull()
        val b2 = benchs.filter { it == JarTestHelper.BenchsWithGroup.bench2 }.firstOrNull()
        val b3 = benchs.filter { it == JarTestHelper.BenchsWithGroup.bench3 }.firstOrNull()

        if (b1 == null || b2 == null || b3 == null) {
            Assertions.fail<String>("Could not extract benchmarks")
        }

        Assertions.assertTrue(b1!!.group == b2!!.group)
        Assertions.assertNull(b3!!.group)
    }

    @Test
    fun testStateObj() {
        val f = AsmBenchFinder(
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource(),
            pkgPrefixes = pkgPrefixes
        )

        val benchs = f.all().getOrElse {
            Assertions.fail<String>("Could not load benchmarks: $it")
            return
        }

        val b1 = benchs.filter { it == JarTestHelper.BenchsStateObj.bench1 }.firstOrNull()
        val b2 = benchs.filter { it == JarTestHelper.BenchsStateObj.bench2 }.firstOrNull()
        val b3 = benchs.filter { it == JarTestHelper.BenchsStateObj.bench3 }.firstOrNull()

        if (b1 == null || b2 == null || b3 == null) {
            Assertions.fail<String>("Could not extract benchmarks")
        }

        Assertions.assertNotEquals(b2!!.jmhParams, b3!!.jmhParams)
    }

    companion object {
        val pkgPrefixes = setOf("org.sample", "org.sam")
    }
}
