package ch.uzh.ifi.seal.bencher.analysis.callgraph

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class SimpleCGPrinterTest {

    @Test
    fun print() {
        val baos = ByteArrayOutputStream()
        val p = SimpleCGPrinter(baos)
        p.print(CGTestHelper.PrinterReader.cgResult)
        val printResult = String(baos.toByteArray())
        val expectedOut = CGTestHelper.PrinterReader.cgOut.readText()
        Assertions.assertEquals(expectedOut, printResult)
    }
}
