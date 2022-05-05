package ch.uzh.ifi.seal.bencher.analysis.coverage

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class SimpleCoveragePrinterTest {

    @Test
    fun print() {
        val baos = ByteArrayOutputStream()
        val p = SimpleCoveragePrinter(baos)
        p.print(CoveragesTestHelper.PrinterReader.coverages)
        val printResult = String(baos.toByteArray())
        val expectedOut = CoveragesTestHelper.PrinterReader.covOut.readText()
        Assertions.assertEquals(expectedOut, printResult)
    }
}
