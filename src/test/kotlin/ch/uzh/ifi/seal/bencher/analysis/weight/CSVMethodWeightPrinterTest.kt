package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
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
        val ws: MethodWeights = mapOf(
                Pair(JarTestHelper.CoreA.m, 1.0),
                Pair(JarTestHelper.CoreB.m, 2.0),
                Pair(JarTestHelper.CoreC.m, 3.0),
                Pair(JarTestHelper.CoreD.m, 4.0)
        )

        val bos = ByteArrayOutputStream()
        val p = CSVMethodWeightPrinter(bos)
        p.print(ws)
        val out = String(bos.toByteArray())
        val lines = out.split("\n")

        Assertions.assertEquals(2 + ws.size, lines.size)
        // header line
        Assertions.assertEquals(header, lines[0])

        val lm = (1 .. 4).map { lines[it] }
        ws.forEach { m, w ->
            val el = MethodWeightTestHelper.csvLine(m, w)
            if (!lm.contains(el)) {
                Assertions.fail<String>("$el not included")
            }
        }

        // last empty line
        Assertions.assertEquals("", lines[5])

    }
}