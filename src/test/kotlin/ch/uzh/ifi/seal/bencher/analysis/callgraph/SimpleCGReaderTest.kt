package ch.uzh.ifi.seal.bencher.analysis.callgraph

import arrow.core.getOrHandle
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SimpleCGReaderTest {
    private val charset = Charsets.UTF_8

    @Test
    fun read() {
        val r = SimpleCGReader(charset = charset)

        val res = r.read(CoveragesTestHelper.PrinterReader.covOut.inputStream())
            .getOrHandle { Assertions.fail<String>("Could not read CG: $it") }
        Assertions.assertEquals(CoveragesTestHelper.PrinterReader.coverages, res)
    }
}
