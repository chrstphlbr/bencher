package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.getOrHandle
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CGMethodWeighterTest {

    @Test
    fun emptyCG() {
        val cg = CGResult(mapOf())
        val mw = CGMethodWeighter(cg = cg)

        val ws = mw.weights().getOrHandle {
            Assertions.fail<String>("Unexpected error value: $it")
            return
        }

        Assertions.assertEquals(ws.size, 0)
    }

    @Test
    fun emptyCGMapper() {
        val cg = CGResult(mapOf())
        val mw = CGMethodWeighter(cg = cg)

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
        val cg = CGResult(mapOf(CGTestHelper.b1Cg, CGTestHelper.b2Cg, CGTestHelper.b3Cg, CGTestHelper.b4Cg))
        val mw = CGMethodWeighter(cg = cg)

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
        val cg = CGResult(mapOf(CGTestHelper.b1Cg, CGTestHelper.b2Cg, CGTestHelper.b3Cg, CGTestHelper.b4Cg))
        val mw = CGMethodWeighter(cg = cg)

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
