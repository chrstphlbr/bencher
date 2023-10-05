package ch.uzh.ifi.seal.bencher.analysis.coverage

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SimpleCoverageReaderTest {
    private val charset = Charsets.UTF_8

    @Test
    fun readAll() {
        val r = SimpleCoverageReader(coverageUnitType = CoverageUnitType.ALL, charset = charset)

        val res = r.read(CoveragesTestHelper.PrinterReader.allCoveragesOut.inputStream())
            .getOrElse {
                Assertions.fail<String>("Could not read coverages: $it")
                return
            }

        Assertions.assertEquals(CoveragesTestHelper.PrinterReader.allCoverages, res)
    }

    @Test
    fun readMethodsfromMethods() {
        val r = SimpleCoverageReader(coverageUnitType = CoverageUnitType.METHOD, charset = charset)

        val res = r.read(CoveragesTestHelper.PrinterReader.methodCoveragesOut.inputStream())
            .getOrElse {
                Assertions.fail<String>("Could not read coverages: $it")
                return
            }
        Assertions.assertEquals(CoveragesTestHelper.PrinterReader.methodCoverages, res)
    }

    @Test
    fun readMethodsfromAll() {
        val r = SimpleCoverageReader(coverageUnitType = CoverageUnitType.METHOD, charset = charset)

        val res = r.read(CoveragesTestHelper.PrinterReader.allCoveragesOut.inputStream())
            .getOrElse {
                Assertions.fail<String>("Could not read coverages: $it")
                return
            }

        Assertions.assertEquals(CoveragesTestHelper.PrinterReader.methodCoverages, res)
    }

    @Test
    fun readLinesfromLines() {
        val r = SimpleCoverageReader(coverageUnitType = CoverageUnitType.LINE, charset = charset)

        val res = r.read(CoveragesTestHelper.PrinterReader.lineCoveragesOut.inputStream())
            .getOrElse {
                Assertions.fail<String>("Could not read coverages: $it")
                return
            }

        Assertions.assertEquals(CoveragesTestHelper.PrinterReader.lineCoverages, res)
    }

    @Test
    fun readLinesfromAll() {
        val r = SimpleCoverageReader(coverageUnitType = CoverageUnitType.LINE, charset = charset)

        val res = r.read(CoveragesTestHelper.PrinterReader.allCoveragesOut.inputStream())
            .getOrElse {
                Assertions.fail<String>("Could not read coverages: $it")
                return
            }
        Assertions.assertEquals(CoveragesTestHelper.PrinterReader.lineCoverages, res)
    }
}
