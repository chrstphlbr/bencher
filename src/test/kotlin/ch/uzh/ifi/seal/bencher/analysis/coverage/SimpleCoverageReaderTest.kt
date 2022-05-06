package ch.uzh.ifi.seal.bencher.analysis.coverage

import arrow.core.getOrHandle
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SimpleCoverageReaderTest {
    private val charset = Charsets.UTF_8

    @Test
    fun read() {
        val r = SimpleCoverageReader(charset = charset)

        val res = r.read(CoveragesTestHelper.PrinterReader.covOut.inputStream())
            .getOrHandle { Assertions.fail<String>("Could not read coverages: $it") }
        Assertions.assertEquals(CoveragesTestHelper.PrinterReader.coverages, res)
    }
}
