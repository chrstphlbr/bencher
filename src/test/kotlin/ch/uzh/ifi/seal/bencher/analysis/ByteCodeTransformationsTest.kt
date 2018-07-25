package ch.uzh.ifi.seal.bencher.analysis

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ByteCodeTransformationsTest {
    fun bcConstantCheck(expectedBc: String, expectedSc: String, constant: Pair<Char, String>) {
        Assertions.assertTrue(constant.first == expectedBc[0], "expected byte code '$expectedBc' but was '${constant.first}'")
        Assertions.assertTrue(constant.second == expectedSc, "expected source code \"$expectedSc\" but was \"${constant.second}\"")
    }

    fun bcToSc(expectedBc: String, expectedSc: String) {
        val sc = expectedBc.toString().sourceCode
        Assertions.assertTrue( sc == expectedSc, "expected \"$expectedSc\" but was \"$sc\"")
    }

    // BaseType tests

    @Test
    fun baseTypeByte() {
        val expectedBc = expectedBcByte
        val expectedSc = expectedScByte
        val c = ByteCodeConstants.byte
        bcConstantCheck(expectedBc, expectedSc, c)
        bcToSc(expectedBc, expectedSc)
    }

    @Test
    fun baseTypeChar() {
        val expectedBc = expectedBcChar
        val expectedSc = expectedScChar
        val c = ByteCodeConstants.char
        bcConstantCheck(expectedBc, expectedSc, c)
        bcToSc(expectedBc, expectedSc)
    }

    @Test
    fun baseTypeDouble() {
        val expectedBc = expectedBcDouble
        val expectedSc = expectedScDouble
        val c = ByteCodeConstants.double
        bcConstantCheck(expectedBc, expectedSc, c)
        bcToSc(expectedBc, expectedSc)
    }

    @Test
    fun baseTypeFloat() {
        val expectedBc = expectedBcFloat
        val expectedSc = expectedScFloat
        val c = ByteCodeConstants.float
        bcConstantCheck(expectedBc, expectedSc, c)
        bcToSc(expectedBc, expectedSc)
    }

    @Test
    fun baseTypeInt() {
        val expectedBc = expectedBcInt
        val expectedSc = expectedScInt
        val c = ByteCodeConstants.int
        bcConstantCheck(expectedBc, expectedSc, c)
        bcToSc(expectedBc, expectedSc)
    }

    @Test
    fun baseTypeLong() {
        val expectedBc = expectedBcLong
        val expectedSc = expectedScLong
        val c = ByteCodeConstants.long
        bcConstantCheck(expectedBc, expectedSc, c)
        bcToSc(expectedBc, expectedSc)
    }

    @Test
    fun baseTypeShort() {
        val expectedBc = expectedBcShort
        val expectedSc = expectedScShort
        val c = ByteCodeConstants.short
        bcConstantCheck(expectedBc, expectedSc, c)
        bcToSc(expectedBc, expectedSc)
    }

    @Test
    fun baseTypeBoolean() {
        val expectedBc = expectedBcBoolean
        val expectedSc = expectedScBoolean
        val c = ByteCodeConstants.boolean
        bcConstantCheck(expectedBc, expectedSc, c)
        bcToSc(expectedBc, expectedSc)
    }

    // ObjectType tests

    @Test
    fun obj() = bcToSc(expectedBcObject, expectedScObject)

    // ArrayType tests

    fun arrayTest(bc: String, sc: String, dimensions: Int) {
        val expectedBc = arrayBc(bc, dimensions)
        val expectedSc = arraySc(sc, dimensions)
        bcToSc(expectedBc, expectedSc)
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
    fun arrayObject(dimensions: Int) = arrayTest(expectedBcObject, expectedScObject, dimensions)

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun arrayObjectSC(dimensions: Int) = arrayTest(expectedBcObjectSC, expectedScObject, dimensions)

    companion object {
        val expectedBcByte = "B"
        val expectedScByte = "byte"

        val expectedBcChar = "C"
        val expectedScChar = "char"

        val expectedBcDouble = "D"
        val expectedScDouble = "double"

        val expectedBcFloat = "F"
        val expectedScFloat = "float"

        val expectedBcInt = "I"
        val expectedScInt = "int"

        val expectedBcLong = "J"
        val expectedScLong = "long"

        val expectedBcShort = "S"
        val expectedScShort = "short"

        val expectedBcBoolean = "Z"
        val expectedScBoolean = "boolean"

        val expectedBcObject = "Lorg/test/Test"
        val expectedBcObjectSC = "$expectedBcObject;"
        val expectedScObject = "org.test.Test"

        fun arrayBc(bc: String, dimensions: Int): String = "[".repeat(dimensions) + bc

        fun arraySc(sc: String, dimensions: Int): String = sc + "[]".repeat(dimensions)
    }

}
