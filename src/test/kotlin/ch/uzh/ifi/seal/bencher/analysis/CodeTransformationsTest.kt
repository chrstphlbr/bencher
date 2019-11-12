package ch.uzh.ifi.seal.bencher.analysis

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class CodeTransformationsTest {
    private fun bcConstantCheck(expectedBc: String, expectedSc: String, bcConstant: Char, scConstant: String) {
        Assertions.assertTrue(bcConstant == expectedBc[0], "expected byte code '$expectedBc' but was '$bcConstant'")
        Assertions.assertTrue(scConstant == expectedSc, "expected source code \"$expectedSc\" but was \"$scConstant\"")
    }

    private fun bcToSc(expectedBc: String, expectedSc: String) {
        val sc = expectedBc.sourceCode
        Assertions.assertTrue(sc == expectedSc, "expected \"$expectedSc\" but was \"$sc\"")
    }

    private fun scToBc(expectedSc: String, expectedBc: String, trailingSemicolon: Boolean = true) {
        val bc = expectedSc.byteCode(trailingSemicolon)
        Assertions.assertTrue(bc == expectedBc, "expected \"$expectedBc\" but was \"$bc\"")
    }

    // BaseType tests

    @Test
    fun baseTypeByte() {
        val expectedBc = expectedBcByte
        val expectedSc = expectedScByte
        bcConstantCheck(expectedBc, expectedSc, ByteCodeConstants.byte, SourceCodeConstants.byte)
        bcToSc(expectedBc, expectedSc)
        scToBc(expectedSc, expectedBc)
    }

    @Test
    fun baseTypeChar() {
        val expectedBc = expectedBcChar
        val expectedSc = expectedScChar
        bcConstantCheck(expectedBc, expectedSc, ByteCodeConstants.char, SourceCodeConstants.char)
        bcToSc(expectedBc, expectedSc)
        scToBc(expectedSc, expectedBc)
    }

    @Test
    fun baseTypeDouble() {
        val expectedBc = expectedBcDouble
        val expectedSc = expectedScDouble
        bcConstantCheck(expectedBc, expectedSc, ByteCodeConstants.double, SourceCodeConstants.double)
        bcToSc(expectedBc, expectedSc)
        scToBc(expectedSc, expectedBc)
    }

    @Test
    fun baseTypeFloat() {
        val expectedBc = expectedBcFloat
        val expectedSc = expectedScFloat
        bcConstantCheck(expectedBc, expectedSc, ByteCodeConstants.float, SourceCodeConstants.float)
        bcToSc(expectedBc, expectedSc)
        scToBc(expectedSc, expectedBc)
    }

    @Test
    fun baseTypeInt() {
        val expectedBc = expectedBcInt
        val expectedSc = expectedScInt
        bcConstantCheck(expectedBc, expectedSc, ByteCodeConstants.int, SourceCodeConstants.int)
        bcToSc(expectedBc, expectedSc)
        scToBc(expectedSc, expectedBc)
    }

    @Test
    fun baseTypeLong() {
        val expectedBc = expectedBcLong
        val expectedSc = expectedScLong
        bcConstantCheck(expectedBc, expectedSc, ByteCodeConstants.long, SourceCodeConstants.long)
        bcToSc(expectedBc, expectedSc)
        scToBc(expectedSc, expectedBc)
    }

    @Test
    fun baseTypeShort() {
        val expectedBc = expectedBcShort
        val expectedSc = expectedScShort
        bcConstantCheck(expectedBc, expectedSc, ByteCodeConstants.short, SourceCodeConstants.short)
        bcToSc(expectedBc, expectedSc)
        scToBc(expectedSc, expectedBc)
    }

    @Test
    fun baseTypeBoolean() {
        val expectedBc = expectedBcBoolean
        val expectedSc = expectedScBoolean
        bcConstantCheck(expectedBc, expectedSc, ByteCodeConstants.boolean, SourceCodeConstants.boolean)
        bcToSc(expectedBc, expectedSc)
        scToBc(expectedSc, expectedBc)
    }

    @Test
    fun baseTypeVoid() {
        val expectedBc = expectedBcVoid
        val expectedSc = expectedScVoid
        bcConstantCheck(expectedBc, expectedSc, ByteCodeConstants.void, SourceCodeConstants.void)
        bcToSc(expectedBc, expectedSc)
        scToBc(expectedSc, expectedBc)
    }

    // ObjectType tests

    @Test
    fun obj() {
        bcToSc(expectedBcObject, expectedScObject)
        bcToSc(expectedBcObjectSC, expectedScObject)
        scToBc(expectedScObject, expectedBcObject, false)
        scToBc(expectedScObject, expectedBcObjectSC, true)
    }

    // ArrayType tests

    private fun arrayTest(bc: String, sc: String, dimensions: Int, trailingSemicolon: Boolean = true) {
        val expectedBc = arrayBc(bc, dimensions)
        val expectedSc = arraySc(sc, dimensions)
        bcToSc(expectedBc, expectedSc)
        scToBc(expectedSc, expectedBc, trailingSemicolon)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun arrayByte(dimensions: Int) = arrayTest(expectedBcByte, expectedScByte, dimensions)

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun arrayChar(dimensions: Int) = arrayTest(expectedBcChar, expectedScChar, dimensions)

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun arrayDouble(dimensions: Int) = arrayTest(expectedBcDouble, expectedScDouble, dimensions)

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun arrayFloat(dimensions: Int) = arrayTest(expectedBcFloat, expectedScFloat, dimensions)

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun arrayInt(dimensions: Int) = arrayTest(expectedBcInt, expectedScInt, dimensions)

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun arrayLong(dimensions: Int) = arrayTest(expectedBcLong, expectedScLong, dimensions)

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun arrayShort(dimensions: Int) = arrayTest(expectedBcShort, expectedScShort, dimensions)

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun arrayBoolean(dimensions: Int) = arrayTest(expectedBcBoolean, expectedScBoolean, dimensions)

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun arrayObject(dimensions: Int) = arrayTest(expectedBcObject, expectedScObject, dimensions, false)

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun arrayObjectSC(dimensions: Int) = arrayTest(expectedBcObjectSC, expectedScObject, dimensions, true)

    companion object {
        val expectedBcByte = "B"
        val expectedScByte = "byte"
        val refByte = "java.lang.Byte"

        val expectedBcChar = "C"
        val expectedScChar = "char"
        val refChar = "java.lang.Character"

        val expectedBcDouble = "D"
        val expectedScDouble = "double"
        val refDouble = "java.lang.Double"

        val expectedBcFloat = "F"
        val expectedScFloat = "float"
        val refFloat = "java.lang.Float"

        val expectedBcInt = "I"
        val expectedScInt = "int"
        val refInt = "java.lang.Integer"

        val expectedBcLong = "J"
        val expectedScLong = "long"
        val refLong = "java.lang.Long"

        val expectedBcShort = "S"
        val expectedScShort = "short"
        val refShort = "java.lang.Short"

        val expectedBcBoolean = "Z"
        val expectedScBoolean = "boolean"
        val refBoolean = "java.lang.Boolean"

        val expectedBcVoid = "V"
        val expectedScVoid = "void"
        val refVoid = "java.lang.Void"

        val expectedBcObject = "Lorg/test/Test"
        val expectedBcObjectSC = "$expectedBcObject;"
        val expectedScObject = "org.test.Test"

        fun arrayBc(bc: String, dimensions: Int): String = "[".repeat(dimensions) + bc

        fun arraySc(sc: String, dimensions: Int): String = sc + "[]".repeat(dimensions)
    }

}
