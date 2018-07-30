package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import com.ibm.wala.ipa.callgraph.Entrypoint
import org.junit.jupiter.api.Assertions

object EntrypointTestHelper {

    object BenchParameterized {
        val entrypoints = listOf(
                Pair(JarTestHelper.BenchParameterized.bench1, EntrypointMock(JarTestHelper.BenchParameterized.bench1)),
                Pair(JarTestHelper.BenchParameterized.setup, EntrypointMock(JarTestHelper.BenchParameterized.setup))
        )
    }

    object BenchNonParameterized {
        val entrypoints = listOf(
                Pair(JarTestHelper.BenchNonParameterized.bench2, EntrypointMock(JarTestHelper.BenchNonParameterized.bench2))
        )
    }

    object OtherBench {
        val entrypoints = listOf(
                Pair(JarTestHelper.OtherBench.bench3, EntrypointMock(JarTestHelper.OtherBench.bench3)),
                Pair(JarTestHelper.OtherBench.setup, EntrypointMock(JarTestHelper.OtherBench.setup)),
                Pair(JarTestHelper.OtherBench.tearDown, EntrypointMock(JarTestHelper.OtherBench.tearDown))
        )
    }

    object BenchParameterized2 {
        val entrypoints = listOf(
                Pair(JarTestHelper.BenchParameterized2.bench4, EntrypointMock(JarTestHelper.BenchParameterized2.bench4)),
                Pair(JarTestHelper.BenchParameterized2.setup, EntrypointMock(JarTestHelper.BenchParameterized2.setup))
        )
    }

    fun validateEntrypoints(eps: List<Pair<Method, Entrypoint>>, expectedEps: List<Pair<Method, Entrypoint>>) {
        val size = expectedEps.size
        val s = eps.size
        Assertions.assertTrue(s == size, "Entrypoint list not of expected ($s) size (was $size)")

        expectedEps.forEach { (m, _) ->
            val c = eps.any { (method, _) ->  m == method}
            Assertions.assertTrue(c, "Entrypoint list does not contain ($m)")
        }
    }

    fun containsEntrypoints(eps: Iterable<Pair<Method, Entrypoint>>, expectedEps: Iterable<Pair<Method, Entrypoint>>): Boolean =
            expectedEps.all { (m, _) -> eps.any { (method, _) ->  m == method} }
}
