package ch.uzh.ifi.seal.bencher.analysis.coverage

import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class SimpleCoveragePrinterTest {

    @Test
    fun printAll() {
        val baos = ByteArrayOutputStream()
        val p = SimpleCoveragePrinter(out = baos, coverageUnitType = CoverageUnitType.ALL)
        p.print(CoveragesTestHelper.PrinterReader.allCoverages)
        val printResult = String(baos.toByteArray())
        val expectedOut = CoveragesTestHelper.PrinterReader.allCoveragesOut.readText()
        Assertions.assertEquals(expectedOut, printResult)
    }

    @Test
    fun printMethodsFromMethods() {
        val baos = ByteArrayOutputStream()
        val p = SimpleCoveragePrinter(out = baos, coverageUnitType = CoverageUnitType.METHOD)
        p.print(CoveragesTestHelper.PrinterReader.methodCoverages)
        val printResult = String(baos.toByteArray())
        val expectedOut = CoveragesTestHelper.PrinterReader.methodCoveragesOut.readText()
        Assertions.assertEquals(expectedOut, printResult)
    }

    @Test
    fun printMethodsFromAll() {
        val baos = ByteArrayOutputStream()
        val p = SimpleCoveragePrinter(out = baos, coverageUnitType = CoverageUnitType.METHOD)
        p.print(CoveragesTestHelper.PrinterReader.allCoverages)
        val printResult = String(baos.toByteArray())
        val expectedOut = CoveragesTestHelper.PrinterReader.methodCoveragesOut.readText()
        Assertions.assertEquals(expectedOut, printResult)
    }

    @Test
    fun printLinesFromLines() {
        val baos = ByteArrayOutputStream()
        val p = SimpleCoveragePrinter(out = baos, coverageUnitType = CoverageUnitType.LINE)
        p.print(CoveragesTestHelper.PrinterReader.lineCoverages)
        val printResult = String(baos.toByteArray())
        val expectedOut = CoveragesTestHelper.PrinterReader.lineCoveragesOut.readText()
        Assertions.assertEquals(expectedOut, printResult)
    }

    @Test
    fun printLinesFromAll() {
        val baos = ByteArrayOutputStream()
        val p = SimpleCoveragePrinter(out = baos, coverageUnitType = CoverageUnitType.LINE)
        p.print(CoveragesTestHelper.PrinterReader.allCoverages)
        val printResult = String(baos.toByteArray())
        val expectedOut = CoveragesTestHelper.PrinterReader.lineCoveragesOut.readText()
        Assertions.assertEquals(expectedOut, printResult)
    }
}
