package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JdtBenchExecInfoFinderTest : AbstractJdtBenchExecInfoTest() {

    @Test
    fun test() {
        val f = JdtBenchFinder(SourceCodeTestHelper.benchs4Jmh121v2.fileResource())

        f.all()

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
        val f = JdtBenchFinder(SourceCodeTestHelper.benchs4Jmh121v2.fileResource())

        val benchs = f.all().getOrElse {
            Assertions.fail<String>("Could not load benchmarks: $it")
            return
        }

        val b1 = benchs.filter { it == SourceCodeTestHelper.BenchsWithGroup.bench1 }.firstOrNull()
        val b2 = benchs.filter { it == SourceCodeTestHelper.BenchsWithGroup.bench2 }.firstOrNull()
        val b3 = benchs.filter { it == SourceCodeTestHelper.BenchsWithGroup.bench3 }.firstOrNull()

        if (b1 == null || b2 == null || b3 == null) {
            Assertions.fail<String>("Could not extract benchmarks")
        }

        Assertions.assertTrue(b1!!.group == b2!!.group)
        Assertions.assertNull(b3!!.group)
    }

    @Test
    fun testStateObj() {
        val f = JdtBenchFinder(SourceCodeTestHelper.benchs4Jmh121v2.fileResource())

        val benchs = f.all().getOrElse {
            Assertions.fail<String>("Could not load benchmarks: $it")
            return
        }

        val b1 = benchs.filter { it == SourceCodeTestHelper.BenchsStateObj.bench1 }.firstOrNull()
        val b2 = benchs.filter { it == SourceCodeTestHelper.BenchsStateObj.bench2 }.firstOrNull()
        val b3 = benchs.filter { it == SourceCodeTestHelper.BenchsStateObj.bench3 }.firstOrNull()

        if (b1 == null || b2 == null || b3 == null) {
            Assertions.fail<String>("Could not extract benchmarks")
        }

        Assertions.assertNotEquals(b2!!.jmhParams, b3!!.jmhParams)
    }
}
