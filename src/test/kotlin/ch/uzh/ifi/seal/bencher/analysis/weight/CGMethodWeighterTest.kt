package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.PossibleMethod
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.MethodCall
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CGMethodWeighterTest {

    @Test
    fun emptyCG() {
        val cg = CGResult(mapOf())
        val mw = CGMethodWeighter(cg = cg)
        val ews = mw.weights()

        if (ews.isLeft()) {
            Assertions.fail<String>("Unexpected error value: ${ews.left().get()}")
        }

        val ws = ews.right().get()
        Assertions.assertEquals(ws.size, 0)
    }

    fun contains(ws: MethodWeights, m: Method) {
        val v = ws[m] ?: Assertions.fail<String>("Method not in weights ($m)")
        Assertions.assertEquals(1.0, v)
    }

    @Test
    fun cg() {
        val cg = CGResult(mapOf(CGTestHelper.b1Cg, CGTestHelper.b2Cg, CGTestHelper.b3Cg, CGTestHelper.b4Cg))
        val mw = CGMethodWeighter(cg = cg)
        val ews = mw.weights()

        if (ews.isLeft()) {
            Assertions.fail<String>("Unexpected error value: ${ews.left().get()}")
        }

        val ws = ews.right().get()
        Assertions.assertEquals(6, ws.size)

        contains(ws, JarTestHelper.CoreA.m)
        contains(ws, JarTestHelper.CoreB.m)
        contains(ws, JarTestHelper.CoreC.m)
        contains(ws, JarTestHelper.CoreD.m)
        contains(ws, PossibleMethod.from(JarTestHelper.CoreE.mn2, 2, 1))
        contains(ws, PossibleMethod.from(JarTestHelper.CoreE.mn1_1, 2, 1))
    }
}
