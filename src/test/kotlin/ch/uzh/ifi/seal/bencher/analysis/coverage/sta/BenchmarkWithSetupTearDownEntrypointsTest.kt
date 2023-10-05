package ch.uzh.ifi.seal.bencher.analysis.coverage.sta

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import com.ibm.wala.ipa.callgraph.AnalysisScope
import com.ibm.wala.ipa.cha.ClassHierarchy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class BenchmarkWithSetupTearDownEntrypointsTest {

    @Test
    fun invalidBenchmark() {
        val eps = eg.entrypoints(AnalysisScope.createJavaAnalysisScope(), cha, Benchmark(
                clazz = "org.sample2.InvalidBench",
                name = "bench1",
                jmhParams = listOf(),
                params = listOf(),
                returnType = SourceCodeConstants.void
        ))

        if (eps.isRight()) {
            Assertions.fail<String>("Got entrypoints for non-existing benchmark")
        }

        Assertions.assertTrue(eps.isLeft())
    }

    @Test
    fun benchOnly() {
        val eps =
            eg.entrypoints(AnalysisScope.createJavaAnalysisScope(), cha, JarTestHelper.BenchNonParameterized.bench2)
                .getOrElse {
                    Assertions.fail<String>("Could not get entrypoints: $it")
                    return
                }

        EntrypointTestHelper.validateEntrypoints(eps.toList(), EntrypointTestHelper.BenchNonParameterized.entrypoints)
    }

    @Test
    fun benchAndSetup() {
        val eps = eg.entrypoints(AnalysisScope.createJavaAnalysisScope(), cha, JarTestHelper.BenchParameterized.bench1)
            .getOrElse {
                Assertions.fail<String>("Could not get entrypoints: $it")
                return
            }

        EntrypointTestHelper.validateEntrypoints(eps.toList(), EntrypointTestHelper.BenchParameterized.entrypoints)
    }

    @Test
    fun benchSetupAndTearDown() {
        val eps = eg.entrypoints(AnalysisScope.createJavaAnalysisScope(), cha, JarTestHelper.OtherBench.bench3)
            .getOrElse {
                Assertions.fail<String>("Could not get entrypoints: $it")
                return
            }

        EntrypointTestHelper.validateEntrypoints(eps.toList(), EntrypointTestHelper.OtherBench.entrypoints)
    }

    companion object {
        val eg = BenchmarkWithSetupTearDownEntrypoints()

        lateinit var cha: ClassHierarchy

        @BeforeAll
        @JvmStatic
        fun setup() {
            cha = WalaSCTestHelper.cha(JarTestHelper.jar4BenchsJmh121)
        }
    }
}
