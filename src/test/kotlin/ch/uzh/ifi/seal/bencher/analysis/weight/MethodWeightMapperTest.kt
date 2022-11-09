package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.toCoverageUnit
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MethodWeightMapperTest {
    private val idm = IdentityMethodWeightMapper
    private val vm = ValueMethodWeightMapper { it * 2 }
    private val log10m = log10CoverageUnitWeightMapper

    private val mw: CoverageUnitWeights =
        mapOf(
            Pair(JarTestHelper.CoreA.m, 1.0),
            Pair(JarTestHelper.CoreB.m, 10.0),
            Pair(JarTestHelper.CoreC.m, 100.0),
            Pair(JarTestHelper.CoreD.m, 1000.0),
            Pair(JarTestHelper.CoreE.mn_1, 10000.0)
        )
            .mapKeys { (k, _) -> k.toCoverageUnit() }

    private val mw2: CoverageUnitWeights =
        mapOf(
            Pair(JarTestHelper.CoreA.m, 5.0),
            Pair(JarTestHelper.CoreB.m, 50.0),
            Pair(JarTestHelper.CoreC.m, 500.0),
            Pair(JarTestHelper.CoreD.m, 5000.0),
            Pair(JarTestHelper.CoreE.mn_1, 50000.0)
        )
            .mapKeys { (k, _) -> k.toCoverageUnit() }

    @Test
    fun identityEmpty() {
        val nmw = idm.map(mapOf())
        Assertions.assertEquals(0, nmw.size)
    }

    @Test
    fun identity() {
        val nmw = idm.map(mw)
        Assertions.assertEquals(mw, nmw)
    }

    @Test
    fun valueEmpty() {
        val nmw = vm.map(mapOf())
        Assertions.assertEquals(0, nmw.size)
    }

    @Test
    fun value() {
        val nmw = vm.map(mw2)
        val emw = mw2.mapValues { it.value * 2 }
        Assertions.assertEquals(emw, nmw)
    }

    private fun digits(d: Double): Double {
        val text = d.toString()
        val integerPlaces = text.indexOf('.')
        return integerPlaces.toDouble()
    }

    @Test
    fun log10Empty() {
        val nmw = log10m.map(mapOf())
        Assertions.assertEquals(0, nmw.size)
    }

    @Test
    fun log10One() {
        val nmw = log10m.map(mw)
        val emw = mw.mapValues { digits(it.value) }
        Assertions.assertEquals(emw, nmw)
    }

    @Test
    fun log10Five() {
        val nmw = log10m.map(mw2)
        val nmwr = nmw.mapValues { Math.round(it.value * 100.0) / 100.0 }
        val emw = mw.mapValues { digits(it.value) + 0.70 }
        Assertions.assertEquals(emw, nmwr)
    }
}
