package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CSVMethodWeighterTest {

    private fun assertWeight(ws: MethodWeights) {
        val aw = ws[JarTestHelper.CoreA.m]
        Assertions.assertNotNull(aw, "CoreA.m weight null")
        Assertions.assertTrue(aw == 1.0)

        val bw = ws[JarTestHelper.CoreB.m]
        Assertions.assertNotNull(bw, "CoreB.m weight null")
        Assertions.assertTrue(bw == 2.0)

        val cw = ws[JarTestHelper.CoreC.m]
        Assertions.assertNotNull(cw, "CoreC.m weight null")
        Assertions.assertTrue(cw == 3.0)

        val dw = ws[JarTestHelper.CoreD.m]
        Assertions.assertNotNull(dw, "CoreD.m weight null")
        Assertions.assertTrue(dw == 4.0)
    }

    @Test
    fun noPrios() {
        val w = CSVMethodWeighter(file = "".byteInputStream())

        val eWeights = w.weights()

        if (eWeights.isLeft()) {
            Assertions.fail<String>("Could not retrieve method weights: ${eWeights.left().get() }}")
        }

        val ws = eWeights.right().get()
        Assertions.assertTrue(ws.isEmpty())
    }

    private fun withPriosTest(prios: String, del: Char, hasHeader: Boolean) {
        val w = CSVMethodWeighter(
                file = prios.byteInputStream(),
                del = del,
                hasHeader = hasHeader,
                hasParams = false
        )

        val eWeights = w.weights()

        if (eWeights.isLeft()) {
            Assertions.fail<String>("Could not retrieve method weights: ${eWeights.left().get() }}")
        }

        val ws = eWeights.right().get()
        Assertions.assertTrue(ws.size == 4)

        assertWeight(ws)
    }

    @ValueSource(chars = [',', ';'])
    @ParameterizedTest
    fun withPrios(del: Char) {
        withPriosTest(
                prios = MethodWeightTestHelper.csvPrios(del),
                del = del,
                hasHeader = false
        )
    }

    @ValueSource(chars = [',', ';'])
    @ParameterizedTest
    fun withPriosHeader(del: Char) {
        withPriosTest(
                prios = MethodWeightTestHelper.csvPriosWithHeader(
                        del,
                        CSVMethodWeighter.csvClass,
                        CSVMethodWeighter.csvMethod,
                        CSVMethodWeighter.csvValue
                ),
                del = del,
                hasHeader = true
        )
    }
}
