package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.getOrHandle
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoveragesTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CoverageMethodWeighterTest {

    @Test
    fun emptyCG() {
        val cg = Coverages(mapOf())
        val mw = CoverageMethodWeighter(cov = cg)

        val ws = mw.weights().getOrHandle {
            Assertions.fail<String>("Unexpected error value: $it")
            return
        }

        Assertions.assertEquals(ws.size, 0)
    }

    @Test
    fun emptyCGMapper() {
        val cg = Coverages(mapOf())
        val mw = CoverageMethodWeighter(cov = cg)

        val ws = mw.weights(MethodWeightTestHelper.doubleMapper).getOrHandle {
            Assertions.fail<String>("Unexpected error value: $it")
            return
        }

        Assertions.assertEquals(ws.size, 0)
    }

    fun contains(ws: MethodWeights, m: Method, f: (Double) -> Double = { it }) {
        val v = ws[m] ?: Assertions.fail<String>("Method not in weights ($m)")
        Assertions.assertEquals(f(1.0), v)
    }

    @Test
    fun cg() {
        val cg = Coverages(mapOf(CoveragesTestHelper.b1Cov, CoveragesTestHelper.b2Cov, CoveragesTestHelper.b3Cov, CoveragesTestHelper.b4Cov))
        val mw = CoverageMethodWeighter(cov = cg)

        val ws = mw.weights().getOrHandle {
            Assertions.fail<String>("Unexpected error value: $it")
            return
        }
        Assertions.assertEquals(6, ws.size)

        contains(ws, JarTestHelper.CoreA.m)
        contains(ws, JarTestHelper.CoreB.m)
        contains(ws, JarTestHelper.CoreC.m)
        contains(ws, JarTestHelper.CoreD.m)
        contains(ws, JarTestHelper.CoreE.mn2)
        contains(ws, JarTestHelper.CoreE.mn1_1)
    }

    @Test
    fun cgMapper() {
        val cg = Coverages(mapOf(CoveragesTestHelper.b1Cov, CoveragesTestHelper.b2Cov, CoveragesTestHelper.b3Cov, CoveragesTestHelper.b4Cov))
        val mw = CoverageMethodWeighter(cov = cg)

        val ws = mw.weights(MethodWeightTestHelper.doubleMapper).getOrHandle {
            Assertions.fail<String>("Unexpected error value: $it")
            return
        }
        Assertions.assertEquals(6, ws.size)

        contains(ws, JarTestHelper.CoreA.m, MethodWeightTestHelper.doubleFun)
        contains(ws, JarTestHelper.CoreB.m, MethodWeightTestHelper.doubleFun)
        contains(ws, JarTestHelper.CoreC.m, MethodWeightTestHelper.doubleFun)
        contains(ws, JarTestHelper.CoreD.m, MethodWeightTestHelper.doubleFun)
        contains(ws, JarTestHelper.CoreE.mn2, MethodWeightTestHelper.doubleFun)
        contains(ws, JarTestHelper.CoreE.mn1_1, MethodWeightTestHelper.doubleFun)
    }
}
