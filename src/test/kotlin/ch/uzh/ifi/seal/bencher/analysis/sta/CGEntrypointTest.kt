package ch.uzh.ifi.seal.bencher.analysis.sta

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.*
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
                        EntrypointTestHelper.OtherBench.entrypoints
        )
    }

    @Test
    fun multipleEntrypoints() {
        val eps = eps(MultiCGEntrypoints()).toList()
        Assertions.assertTrue(eps.size == 3, "Not a multiple entry point list")

        val expectedEps1 = EntrypointTestHelper.BenchParameterized.entrypoints
        val expectedEps2 = EntrypointTestHelper.BenchNonParameterized.entrypoints
        val expectedEps3 = EntrypointTestHelper.OtherBench.entrypoints

        val saw = Array(3) { false }

        eps.forEach {
            saw[0] = saw[0] || EntrypointTestHelper.containsEntrypoints(it, expectedEps1)
            saw[1] = saw[1] || EntrypointTestHelper.containsEntrypoints(it, expectedEps2)
            saw[2] = saw[2] || EntrypointTestHelper.containsEntrypoints(it, expectedEps3)
        }
        
        Assertions.assertTrue(saw.all { it }, "Not all expected Entrypoints found")
    }


    fun eps(ea: EntrypointsAssembler): Iterable<Iterable<Pair<Method, Entrypoint>>> {
        val epsg = CGEntrypoints(
                mf = JarBenchFinder(jarFile.absolutePath),
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
        val jarFile = JarHelper.jar3BenchsJmh121.fileResource()

        lateinit var cha: ClassHierarchy

        @BeforeAll
        @JvmStatic
        fun steup() {
            cha = WalaSCGTestHelper.cha(JarHelper.jar3BenchsJmh121)
        }
    }
}
