package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.PlainMethod
import com.ibm.wala.ipa.callgraph.Entrypoint
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class EntrypointsAssemblerTest {

    @Test
    fun single() {
        val epa = SingleCGEntrypoints()
        val cgEps = epa.assemble(eps).toList()

        Assertions.assertTrue(cgEps.size == 1, "Not a single entrypoint list")

        val sEps = cgEps.get(0).toList()
        val sEpsSize = sEps.size
        Assertions.assertTrue(sEpsSize == epsTotalSize, "Single entrypoint list does not contain all elements: $sEpsSize of $epsTotalSize")

        contains(sEps, "p1", p1)
        contains(sEps, "p2", p2)
        contains(sEps, "p3", p3)
        contains(sEps, "p4", p4)
        contains(sEps, "p5", p5)
        contains(sEps, "p6", p6)
        contains(sEps, "p7", p7)
    }

    fun contains(c: List<Pair<Method, Entrypoint>>, n: String, p: Pair<Method, Entrypoint>) {
        val cp = c.contains(p)
        Assertions.assertTrue(cp, "Does not contain $n ($p)")
    }

    @Test
    fun multiple() {
        val epa = MultiCGEntrypoints()
        val cgEps = epa.assemble(eps).toList()

        Assertions.assertTrue(cgEps.size == 4, "Not a multiple entrypoint list")

        val eps1 = cgEps.get(0).toList()
        checkSubList(eps1, "First", 2, listOf(p1, p2))

        val eps2 = cgEps.get(1).toList()
        checkSubList(eps2, "First", 2, listOf(p3, p4))

        val eps3 = cgEps.get(2).toList()
        checkSubList(eps3, "First", 2, listOf(p5, p6))

        val eps4 = cgEps.get(3).toList()
        checkSubList(eps4, "First", 1, listOf(p7))
    }

    fun checkSubList(c: List<Pair<Method, Entrypoint>>, n: String, size: Int, elements: List<Pair<Method, Entrypoint>>) {
        val epsSize = c.size
        Assertions.assertTrue(epsSize == size, "$n multiple entrypoint list invalid element list: $epsSize of $size")

        elements.forEachIndexed { i, p ->
            contains(c, "$n - $i", p)
        }
    }

    companion object {
        val b1 = Benchmark(clazz = "c1", name = "b1", params = listOf(), jmhParams = listOf())
        val b2 = Benchmark(clazz = "c1", name = "b2", params = listOf(), jmhParams = listOf())
        val b3 = Benchmark(clazz = "c2", name = "b3", params = listOf(), jmhParams = listOf())
        val b4 = Benchmark(clazz = "c3", name = "b4", params = listOf(), jmhParams = listOf())

        val s1 = PlainMethod(clazz = "c1", name = "s1", params = listOf())
        val s2 = PlainMethod(clazz = "c2", name = "s2", params = listOf())

        val epb1 = dep(b1)
        val epb2 = dep(b2)
        val epb3 = dep(b3)
        val epb4 = dep(b4)

        val eps1 = dep(s1)
        val eps2 = dep(s2)

        val p1 = Pair(b1, epb1)
        val p2 = Pair(s1, eps1)
        val p3 = Pair(b2, epb2)
        val p4 = Pair(s1, eps1)
        val p5 = Pair(b3, epb3)
        val p6 = Pair(s2, eps2)
        val p7 = Pair(b4, epb4)

        val epsTotalSize = 7

        val eps = sequenceOf(
                sequenceOf(p1, p2),
                sequenceOf(p3, p4),
                sequenceOf(p5, p6),
                sequenceOf(p7)
        )

        fun dep(m: Method): Entrypoint = EntrypointMock(m)
    }
}