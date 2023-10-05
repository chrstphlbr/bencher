package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoveragesTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.toCoverageUnit
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CoveragesWeighterTest {

    @Test
    fun emptyCoverages() {
        val cov = Coverages(mapOf())
        val mw = CoveragesWeighter(cov = cov)

        val ws = mw.weights().getOrElse {
            Assertions.fail<String>("Unexpected error value: $it")
            return
        }

        Assertions.assertEquals(ws.size, 0)
    }

    @Test
    fun emptyCoveragesMapper() {
        val cov = Coverages(mapOf())
        val mw = CoveragesWeighter(cov = cov)

        val ws = mw.weights(MethodWeightTestHelper.doubleMapper).getOrElse {
            Assertions.fail<String>("Unexpected error value: $it")
            return
        }

        Assertions.assertEquals(ws.size, 0)
    }

    fun contains(ws: CoverageUnitWeights, m: Method, f: (Double) -> Double = { it }) {
        val v = ws[m.toCoverageUnit()] ?: Assertions.fail<String>("Method not in weights ($m)")
        Assertions.assertEquals(f(1.0), v)
    }

    @Test
    fun coverages() {
        val cov = Coverages(
            mapOf(
                CoveragesTestHelper.b1MethodCov,
                CoveragesTestHelper.b2MethodCov,
                CoveragesTestHelper.b3MethodCov,
                CoveragesTestHelper.b4MethodCov
            )
        )
        val mw = CoveragesWeighter(cov = cov)

        val ws = mw.weights().getOrElse {
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
    fun coveragesMapper() {
        val cov = Coverages(
            mapOf(
                CoveragesTestHelper.b1MethodCov,
                CoveragesTestHelper.b2MethodCov,
                CoveragesTestHelper.b3MethodCov,
                CoveragesTestHelper.b4MethodCov
            )
        )
        val mw = CoveragesWeighter(cov = cov)

        val ws = mw.weights(MethodWeightTestHelper.doubleMapper).getOrElse {
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
