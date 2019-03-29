package ch.uzh.ifi.seal.bencher.analysis.callgraph

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SimpleCGReaderTest {
    private val charset = "UTF-8"

    @Test
    fun read() {
        val r = SimpleCGReader(charset = charset)
        val eRes = r.read(CGTestHelper.PrinterReader.cgOut.inputStream())
        if (eRes.isLeft()) {
            Assertions.fail<String>("Could not read CG: ${eRes.left().get()}")
        }

        val res = eRes.right().get()
        Assertions.assertEquals(CGTestHelper.PrinterReader.cgResult, res)
    }
}
