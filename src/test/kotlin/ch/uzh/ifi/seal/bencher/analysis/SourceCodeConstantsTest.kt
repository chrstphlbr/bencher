package ch.uzh.ifi.seal.bencher.analysis

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SourceCodeConstantsTest {
    // primitive conversions
    fun wideningBoolean() {
        val expected = setOf<String>()
        val r = SourceCodeConstants.wideningPrimitiveConversion(SourceCodeConstants.boolean)
        Assertions.assertEquals(expected, r)
    }

    fun wideningVoid() {
        val expected = setOf<String>()
        val r = SourceCodeConstants.wideningPrimitiveConversion(SourceCodeConstants.void)
        Assertions.assertEquals(expected, r)
    }

    fun wideningByte() {
        val expected = setOf(SourceCodeConstants.short, SourceCodeConstants.int, SourceCodeConstants.long, SourceCodeConstants.float, SourceCodeConstants.double)
        val r = SourceCodeConstants.wideningPrimitiveConversion(SourceCodeConstants.byte)
        Assertions.assertEquals(expected, r)
    }

    fun wideningChar() {
        val expected = setOf(SourceCodeConstants.int, SourceCodeConstants.long, SourceCodeConstants.float, SourceCodeConstants.double)
        val r = SourceCodeConstants.wideningPrimitiveConversion(SourceCodeConstants.char)
        Assertions.assertEquals(expected, r)
    }

    fun wideningDouble() {
        val expected = setOf<String>()
        val r = SourceCodeConstants.wideningPrimitiveConversion(SourceCodeConstants.double)
        Assertions.assertEquals(expected, r)
    }

    fun wideningFloat() {
        val expected = setOf(SourceCodeConstants.double)
        val r = SourceCodeConstants.wideningPrimitiveConversion(SourceCodeConstants.float)
        Assertions.assertEquals(expected, r)
    }

    fun wideningInt() {
        val expected = setOf(SourceCodeConstants.long, SourceCodeConstants.float, SourceCodeConstants.double)
        val r = SourceCodeConstants.wideningPrimitiveConversion(SourceCodeConstants.int)
        Assertions.assertEquals(expected, r)
    }

    fun wideningLong() {
        val expected = setOf(SourceCodeConstants.float, SourceCodeConstants.double)
        val r = SourceCodeConstants.wideningPrimitiveConversion(SourceCodeConstants.long)
        Assertions.assertEquals(expected, r)
    }

    fun wideningShort() {
        val expected = setOf(SourceCodeConstants.int, SourceCodeConstants.long, SourceCodeConstants.float, SourceCodeConstants.double)
        val r = SourceCodeConstants.wideningPrimitiveConversion(SourceCodeConstants.short)
        Assertions.assertEquals(expected, r)
    }

    fun narrowingBoolean() {
        val expected = setOf<String>()
        val r = SourceCodeConstants.narrowingPrimitiveConversion(SourceCodeConstants.boolean)
        Assertions.assertEquals(expected, r)
    }

    fun narrowingVoid() {
        val expected = setOf<String>()
        val r = SourceCodeConstants.narrowingPrimitiveConversion(SourceCodeConstants.void)
        Assertions.assertEquals(expected, r)
    }

    fun narrowingByte() {
        val expected = setOf<String>()
        val r = SourceCodeConstants.narrowingPrimitiveConversion(SourceCodeConstants.byte)
        Assertions.assertEquals(expected, r)
    }

    fun narrowingChar() {
        val expected = setOf<String>(SourceCodeConstants.byte, SourceCodeConstants.short)
        val r = SourceCodeConstants.narrowingPrimitiveConversion(SourceCodeConstants.char)
        Assertions.assertEquals(expected, r)
    }

    fun narrowingDouble() {
        val expected = setOf<String>(SourceCodeConstants.byte, SourceCodeConstants.short, SourceCodeConstants.char, SourceCodeConstants.int, SourceCodeConstants.long, SourceCodeConstants.float)
        val r = SourceCodeConstants.narrowingPrimitiveConversion(SourceCodeConstants.double)
        Assertions.assertEquals(expected, r)
    }

    fun narrowingFloat() {
        val expected = setOf<String>(SourceCodeConstants.byte, SourceCodeConstants.short, SourceCodeConstants.char, SourceCodeConstants.int, SourceCodeConstants.long)
        val r = SourceCodeConstants.narrowingPrimitiveConversion(SourceCodeConstants.float)
        Assertions.assertEquals(expected, r)
    }

    fun narrowingInt() {
        val expected = setOf<String>(SourceCodeConstants.byte, SourceCodeConstants.short, SourceCodeConstants.char)
        val r = SourceCodeConstants.narrowingPrimitiveConversion(SourceCodeConstants.int)
        Assertions.assertEquals(expected, r)
    }

    fun narrowingLong() {
        val expected = setOf<String>(SourceCodeConstants.byte, SourceCodeConstants.short, SourceCodeConstants.char, SourceCodeConstants.int)
        val r = SourceCodeConstants.narrowingPrimitiveConversion(SourceCodeConstants.long)
        Assertions.assertEquals(expected, r)
    }

    fun narrowingShort() {
        val expected = setOf<String>(SourceCodeConstants.byte, SourceCodeConstants.char)
        val r = SourceCodeConstants.narrowingPrimitiveConversion(SourceCodeConstants.short)
        Assertions.assertEquals(expected, r)
    }


    // boxed type tests
    private fun checkBoxing(expectedUnboxed: String, expectedBoxed: String) {
        val b = SourceCodeConstants.boxedType(expectedUnboxed)
        Assertions.assertNotNull(b)
        Assertions.assertEquals(expectedBoxed, b)
        val ub = SourceCodeConstants.unboxedType(expectedBoxed)
        Assertions.assertNotNull(ub)
        Assertions.assertEquals(expectedUnboxed, ub)
        val uqub = SourceCodeConstants.unboxedType(expectedBoxed.substringAfterLast("."), true)
        Assertions.assertNotNull(uqub)
        Assertions.assertEquals(expectedUnboxed, uqub)
    }

    @Test
    fun boxingTypeByte() {
        checkBoxing(SourceCodeConstants.byte, SourceCodeConstants.refByte)
    }

    @Test
    fun boxingTypeChar() {
        checkBoxing(SourceCodeConstants.char, SourceCodeConstants.refChar)
    }

    @Test
    fun boxingTypeDouble() {
        checkBoxing(SourceCodeConstants.double, SourceCodeConstants.refDouble)
    }

    @Test
    fun boxingTypeFloat() {
        checkBoxing(SourceCodeConstants.float, SourceCodeConstants.refFloat)
    }

    @Test
    fun boxingTypeInt() {
        checkBoxing(SourceCodeConstants.int, SourceCodeConstants.refInt)
    }

    @Test
    fun boxingTypeLong() {
        checkBoxing(SourceCodeConstants.long, SourceCodeConstants.refLong)
    }

    @Test
    fun boxingTypeShort() {
        checkBoxing(SourceCodeConstants.short, SourceCodeConstants.refShort)
    }

    @Test
    fun boxingTypeBoolean() {
        checkBoxing(SourceCodeConstants.boolean, SourceCodeConstants.refBoolean)
    }

    @Test
    fun boxingTypeVoid() {
        checkBoxing(SourceCodeConstants.void, SourceCodeConstants.refVoid)
    }
}
