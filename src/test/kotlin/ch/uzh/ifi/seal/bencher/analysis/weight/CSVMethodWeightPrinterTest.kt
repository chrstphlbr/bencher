package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitMethod
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.toCoverageUnit
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class CSVMethodWeightPrinterTest {
    private val header = "class;method;params;value"

    @Test
    fun empty() {
        val bos = ByteArrayOutputStream()
        val p = CSVMethodWeightPrinter(bos)
        p.print(mapOf())
        val out = String(bos.toByteArray())
        val lines = out.split("\n")
        Assertions.assertEquals(2, lines.size)
        // header line
        Assertions.assertEquals(header, lines[0])
        // last empty line
        Assertions.assertEquals("", lines[1])
    }

    @Test
    fun multiple() {
        val ws: CoverageUnitWeights =
            mapOf(
                Pair(JarTestHelper.CoreA.m.copy(params = listOf()), 1.0),
                Pair(JarTestHelper.CoreB.m.copy(params = listOf("java.lang.String")), 2.0),
                Pair(JarTestHelper.CoreC.m.copy(params = listOf("int", "float")), 3.0),
                Pair(JarTestHelper.CoreD.m.copy(params = listOf("long", "java.util.Integer", "java.lang.String")), 4.0)
            )
            .mapKeys { (k, _) -> k.toCoverageUnit() }

        val bos = ByteArrayOutputStream()
        val p = CSVMethodWeightPrinter(bos)
        p.print(ws)
        val out = String(bos.toByteArray())
        val lines = out.split("\n")

        Assertions.assertEquals(2 + ws.size, lines.size)
        // header line
        Assertions.assertEquals(header, lines[0])

        val lm = (1..4).map { lines[it] }

        ws.forEach { (u, w) ->
            val m = when (u) {
                is CoverageUnitMethod -> u.method
                else -> Assertions.fail("did not get a CoverageUnitMethod $u")
            }
            val el = MethodWeightTestHelper.csvLine(m, w)
            if (!lm.contains(el)) {
                Assertions.fail<String>("$el not included")
            }
        }

        // last empty line
        Assertions.assertEquals("", lines[5])

    }
}