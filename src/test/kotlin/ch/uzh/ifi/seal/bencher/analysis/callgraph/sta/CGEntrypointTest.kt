package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.finder.JarBenchFinder
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.ipa.callgraph.Entrypoint
import com.ibm.wala.ipa.cha.ClassHierarchy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class CGEntrypointTest {

    @Test
    fun singleEntrypoints() {
        val eps = eps(SingleCGEntrypoints()).toList()
        Assertions.assertTrue(eps.size == 1, "Not a single entry point list")

        EntrypointTestHelper.validateEntrypoints(
                eps.get(0).toList(),
                EntrypointTestHelper.BenchParameterized.entrypoints +
                        EntrypointTestHelper.BenchNonParameterized.entrypoints +
                        EntrypointTestHelper.OtherBench.entrypoints +
                        EntrypointTestHelper.BenchParameterized2.entrypoints
        )
    }

    @Test
    fun multipleEntrypoints() {
        val eps = eps(MultiCGEntrypoints()).toList()
        Assertions.assertTrue(eps.size == 4, "Not a multiple entry point list")

        val expectedEps1 = EntrypointTestHelper.BenchParameterized.entrypoints
        val expectedEps2 = EntrypointTestHelper.BenchNonParameterized.entrypoints
        val expectedEps3 = EntrypointTestHelper.OtherBench.entrypoints
        val expectedEps4 = EntrypointTestHelper.BenchParameterized2.entrypoints

        val saw = Array(4) { false }

        eps.forEach {
            saw[0] = saw[0] || EntrypointTestHelper.containsEntrypoints(it, expectedEps1)
            saw[1] = saw[1] || EntrypointTestHelper.containsEntrypoints(it, expectedEps2)
            saw[2] = saw[2] || EntrypointTestHelper.containsEntrypoints(it, expectedEps3)
            saw[3] = saw[3] || EntrypointTestHelper.containsEntrypoints(it, expectedEps4)
        }

        Assertions.assertTrue(saw.all { it }, "Not all expected Entrypoints found")
    }


    fun eps(ea: EntrypointsAssembler): Iterable<Iterable<Pair<Method, Entrypoint>>> {
        val epsg = CGEntrypoints(
                mf = JarBenchFinder(jarFile.toPath()),
                ea = ea,
                me = BenchmarkWithSetupTearDownEntrypoints()
        )

        val eps = epsg.generate(cha)
        if (eps.isLeft()) {
            Assertions.fail<String>("Could not get entry points: ${eps.left().get()}")
        }

        return eps.right().get()
    }

    companion object {
        val jarFile = JarTestHelper.jar4BenchsJmh121.fileResource()

        lateinit var cha: ClassHierarchy

        @BeforeAll
        @JvmStatic
        fun setup() {
            cha = WalaSCGTestHelper.cha(JarTestHelper.jar4BenchsJmh121)
        }
    }
}
