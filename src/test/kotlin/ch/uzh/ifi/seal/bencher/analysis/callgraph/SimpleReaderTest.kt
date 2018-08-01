package ch.uzh.ifi.seal.bencher.analysis.callgraph

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.charset.Charset

class SimpleReaderTest {
    private val charset = "UTF-8"

    @Test
    fun readDefaultIndent() {
        val r = SimpleReader(charset = charset)
        val eRes = r.read(CGTestHelper.PrinterReader.expectedOut().byteInputStream(Charset.forName(charset)))
        if (eRes.isLeft()) {
            Assertions.fail<String>("Could not read CG: ${eRes.left().get()}")
        }

        val res = eRes.right().get()
        Assertions.assertTrue(CGTestHelper.PrinterReader.cgResult == res)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun read(i: Int) {
        val indent = " ".repeat(i)
        val r = SimpleReader(indent = indent, charset = charset)
        val eRes = r.read(CGTestHelper.PrinterReader.expectedOut(indent).byteInputStream(Charset.forName(charset)))
        if (eRes.isLeft()) {
            Assertions.fail<String>("Could not read CG: ${eRes.left().get()}")
        }

        val res = eRes.right().get()
        Assertions.assertTrue(CGTestHelper.PrinterReader.cgResult == res)
    }
}
