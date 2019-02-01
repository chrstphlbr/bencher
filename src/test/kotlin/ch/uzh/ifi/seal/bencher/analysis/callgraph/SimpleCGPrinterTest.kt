package ch.uzh.ifi.seal.bencher.analysis.callgraph

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayOutputStream

class SimpleCGPrinterTest {
    @Test
    fun printDefaultIndent() {
       val baos = ByteArrayOutputStream()
       val p = SimpleCGPrinter(baos)
       p.print(CGTestHelper.PrinterReader.cgResult)
       val printResult = String(baos.toByteArray())
       Assertions.assertTrue(printResult == CGTestHelper.PrinterReader.expectedOut())
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun print(i: Int) {
        val indent = " ".repeat(i)
        val baos = ByteArrayOutputStream()
        val p = SimpleCGPrinter(baos, indent = indent)
        p.print(CGTestHelper.PrinterReader.cgResult)
        val printResult = String(baos.toByteArray())
        Assertions.assertTrue(printResult == CGTestHelper.PrinterReader.expectedOut(indent))
    }
}
