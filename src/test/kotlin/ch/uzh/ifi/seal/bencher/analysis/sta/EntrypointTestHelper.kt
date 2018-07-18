package ch.uzh.ifi.seal.bencher.analysis.sta

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import com.ibm.wala.ipa.callgraph.Entrypoint
import org.junit.jupiter.api.Assertions

object EntrypointTestHelper {

    object BenchParameterized {
        val entrypoints = listOf(
                Pair(JarHelper.BenchParameterized.bench1, EntrypointMock(JarHelper.BenchParameterized.bench1)),
                Pair(JarHelper.BenchParameterized.setup, EntrypointMock(JarHelper.BenchParameterized.setup))
        )
    }

    object BenchNonParameterized {
        val entrypoints = listOf(
                Pair(JarHelper.BenchNonParameterized.bench2, EntrypointMock(JarHelper.BenchNonParameterized.bench2))
        )
    }

    object OtherBench {
        val entrypoints = listOf(
                Pair(JarHelper.OtherBench.bench3, EntrypointMock(JarHelper.OtherBench.bench3)),
                Pair(JarHelper.OtherBench.setup, EntrypointMock(JarHelper.OtherBench.setup)),
                Pair(JarHelper.OtherBench.tearDown, EntrypointMock(JarHelper.OtherBench.tearDown))
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
