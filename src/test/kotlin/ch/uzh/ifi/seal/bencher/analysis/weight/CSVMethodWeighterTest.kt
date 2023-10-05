package ch.uzh.ifi.seal.bencher.analysis.weight

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.toCoverageUnit
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CSVMethodWeighterTest {

    private fun assertWeight(ws: CoverageUnitWeights, m: (Double) -> Double = { it }) {
        val aw = ws[JarTestHelper.CoreA.m.toCoverageUnit()]
        Assertions.assertNotNull(aw, "CoreA.m weight null")
        Assertions.assertEquals(m(1.0), aw)

        val bw = ws[JarTestHelper.CoreB.m.toCoverageUnit()]
        Assertions.assertNotNull(bw, "CoreB.m weight null")
        Assertions.assertEquals(m(2.0), bw)

        val cw = ws[JarTestHelper.CoreC.m.toCoverageUnit()]
        Assertions.assertNotNull(cw, "CoreC.m weight null")
        Assertions.assertEquals(m(3.0), cw)

        val dw = ws[JarTestHelper.CoreD.m.toCoverageUnit()]
        Assertions.assertNotNull(dw, "CoreD.m weight null")
        Assertions.assertEquals(m(4.0), dw)

        val ew = ws[MethodWeightTestHelper.coreEmParams.toCoverageUnit()]
        Assertions.assertNotNull(ew, "CoreE.mn1_1 weight null")
        Assertions.assertEquals(m(5.0), ew)
    }

    private fun assertWeightWithParams(ws: CoverageUnitWeights, m: (Double) -> Double = { it }) {
        val aw = ws[MethodWeightTestHelper.coreAmParams.toCoverageUnit()]
        Assertions.assertNotNull(aw, "CoreA.m weight null")
        Assertions.assertEquals(m(1.0), aw)

        val bw = ws[MethodWeightTestHelper.coreBmParams.toCoverageUnit()]
        Assertions.assertNotNull(bw, "CoreB.m weight null")
        Assertions.assertEquals(m(2.0), bw)

        val cw = ws[MethodWeightTestHelper.coreCmParams.toCoverageUnit()]
        Assertions.assertNotNull(cw, "CoreC.m weight null")
        Assertions.assertEquals(m(3.0), cw)

        val dw = ws[MethodWeightTestHelper.coreDmParams.toCoverageUnit()]
        Assertions.assertNotNull(dw, "CoreD.m weight null")
        Assertions.assertEquals(m(4.0), dw)

        val ew = ws[MethodWeightTestHelper.coreEmParams.toCoverageUnit()]
        Assertions.assertNotNull(ew, "CoreE.mn1_1 weight null")
        Assertions.assertEquals(m(5.0), ew)
    }

    @Test
    fun noPrios() {
        val w = CSVMethodWeighter(file = "".byteInputStream())

        val ws = w.weights().getOrElse {
            Assertions.fail<String>("Could not retrieve method weights: $it")
            return
        }
        Assertions.assertTrue(ws.isEmpty())
    }

    @Test
    fun noPriosMapper() {
        val w = CSVMethodWeighter(file = "".byteInputStream())

        val ws = w.weights(MethodWeightTestHelper.doubleMapper).getOrElse {
            Assertions.fail<String>("Could not retrieve method weights: $it")
            return
        }
        Assertions.assertTrue(ws.isEmpty())
    }

    private fun withPriosTest(
        prios: String,
        del: Char,
        hasHeader: Boolean,
        hasParams: Boolean,
        m: CoverageUnitWeightMapper? = null,
        mf: (Double) -> Double = { it }
    ) {
        val w = CSVMethodWeighter(
            file = prios.byteInputStream(),
            del = del,
            hasHeader = hasHeader,
            hasParams = hasParams
        )

        val eWeights = if (m == null) {
            w.weights()
        } else {
            w.weights(m)
        }

        val ws = eWeights.getOrElse {
            Assertions.fail<String>("Could not retrieve method weights: $it")
            return
        }
        Assertions.assertEquals(5, ws.size)

        if (hasParams) {
            assertWeightWithParams(ws, mf)
        } else {
            assertWeight(ws, mf)
        }
    }

    @ValueSource(chars = [',', ';'])
    @ParameterizedTest
    fun withPrios(del: Char) {
        withPriosTest(
                prios = MethodWeightTestHelper.csvPrios(del),
                del = del,
                hasHeader = false,
                hasParams = false
        )
    }

    @ValueSource(chars = [',', ';'])
    @ParameterizedTest
    fun withPriosMapper(del: Char) {
        withPriosTest(
                prios = MethodWeightTestHelper.csvPrios(del),
                del = del,
                hasHeader = false,
                hasParams = false,
                m = MethodWeightTestHelper.doubleMapper,
                mf = MethodWeightTestHelper.doubleFun
        )
    }

    @ValueSource(chars = [',', ';'])
    @ParameterizedTest
    fun withPriosHeader(del: Char) {
        withPriosTest(
                prios = MethodWeightTestHelper.csvPriosWithHeader(
                        del,
                        false,
                        CSVMethodWeightConstants.clazz,
                        CSVMethodWeightConstants.method,
                        CSVMethodWeightConstants.value
                ),
                del = del,
                hasHeader = true,
                hasParams = false
        )
    }

    @ValueSource(chars = [',', ';'])
    @ParameterizedTest
    fun withPriosHeaderMapper(del: Char) {
        withPriosTest(
                prios = MethodWeightTestHelper.csvPriosWithHeader(
                        del,
                        false,
                        CSVMethodWeightConstants.clazz,
                        CSVMethodWeightConstants.method,
                        CSVMethodWeightConstants.value
                ),
                del = del,
                hasHeader = true,
                hasParams = false,
                m = MethodWeightTestHelper.doubleMapper,
                mf = MethodWeightTestHelper.doubleFun
        )
    }

    @ValueSource(chars = [';'])
    @ParameterizedTest
    fun withPriosParams(del: Char) {
        withPriosTest(
                prios = MethodWeightTestHelper.csvPrios(del, true),
                del = del,
                hasHeader = false,
                hasParams = true
        )
    }

    @ValueSource(chars = [';'])
    @ParameterizedTest
    fun withPriosParamsMapper(del: Char) {
        withPriosTest(
                prios = MethodWeightTestHelper.csvPrios(del, true),
                del = del,
                hasHeader = false,
                hasParams = true,
                m = MethodWeightTestHelper.doubleMapper,
                mf = MethodWeightTestHelper.doubleFun
        )
    }

    @ValueSource(chars = [';'])
    @ParameterizedTest
    fun withPriosHeaderAndParams(del: Char) {
        withPriosTest(
                prios = MethodWeightTestHelper.csvPriosWithHeader(
                        del,
                        true,
                        CSVMethodWeightConstants.clazz,
                        CSVMethodWeightConstants.method,
                        CSVMethodWeightConstants.params,
                        CSVMethodWeightConstants.value
                ),
                del = del,
                hasHeader = true,
                hasParams = true
        )
    }

    @ValueSource(chars = [';'])
    @ParameterizedTest
    fun withPriosHeaderAndParamsMapper(del: Char) {
        withPriosTest(
                prios = MethodWeightTestHelper.csvPriosWithHeader(
                        del,
                        true,
                        CSVMethodWeightConstants.clazz,
                        CSVMethodWeightConstants.method,
                        CSVMethodWeightConstants.params,
                        CSVMethodWeightConstants.value
                ),
                del = del,
                hasHeader = true,
                hasParams = true,
                m = MethodWeightTestHelper.doubleMapper,
                mf = MethodWeightTestHelper.doubleFun
        )
    }
}
