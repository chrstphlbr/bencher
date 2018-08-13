package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import com.ibm.wala.ipa.cha.ClassHierarchy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class BenchmarkWithSetupTearDownEntrypointsTest {

    @Test
    fun invalidBenchmark() {
        val eps = eg.entrypoints(cha, Benchmark(
                clazz = "org.sample2.InvalidBench",
                name = "bench1",
                jmhParams = listOf(),
                params = listOf()
        ))

        if (eps.isRight()) {
            Assertions.fail<String>("Got entrypoints for non-existing benchmark")
        }

        Assertions.assertTrue(eps.isLeft())
    }

    @Test
    fun benchOnly() {
        val eps = eg.entrypoints(cha, JarTestHelper.BenchNonParameterized.bench2)
        if (eps.isLeft()) {
            Assertions.fail<String>("Could not get entrypoints: ${eps.left().get()}")
        }

        EntrypointTestHelper.validateEntrypoints(eps.right().get().toList(), EntrypointTestHelper.BenchNonParameterized.entrypoints)
    }

    @Test
    fun benchAndSetup() {
        val eps = eg.entrypoints(cha, JarTestHelper.BenchParameterized.bench1)
        if (eps.isLeft()) {
            Assertions.fail<String>("Could not get entrypoints: ${eps.left().get()}")
        }

        EntrypointTestHelper.validateEntrypoints(eps.right().get().toList(), EntrypointTestHelper.BenchParameterized.entrypoints)
    }

    @Test
    fun benchSetupAndTearDown() {
        val eps = eg.entrypoints(cha, JarTestHelper.OtherBench.bench3)
        if (eps.isLeft()) {
            Assertions.fail<String>("Could not get entrypoints: ${eps.left().get()}")
        }

        EntrypointTestHelper.validateEntrypoints(eps.right().get().toList(), EntrypointTestHelper.OtherBench.entrypoints)
    }

    companion object {
        val eg = BenchmarkWithSetupTearDownEntrypoints()

        lateinit var cha: ClassHierarchy

        @BeforeAll
        @JvmStatic
        fun setup() {
            cha = WalaSCGTestHelper.cha(JarTestHelper.jar4BenchsJmh121)
        }
    }
}
