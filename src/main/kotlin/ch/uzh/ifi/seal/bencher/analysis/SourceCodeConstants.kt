package ch.uzh.ifi.seal.bencher.analysis

object SourceCodeConstants {
    const val boolean = "boolean"
    const val byte = "byte"
    const val char = "char"
    const val double = "double"
    const val float = "float"
    const val int = "int"
    const val long = "long"
    const val short = "short"
    const val void = "void"

    val primitives = setOf(boolean, byte, char, double, float, int, long, short, void)

    fun wideningPrimitiveConversion(s: String): Set<String> =
            when (s) {
                byte -> setOf(short, int, long, float, double)
                short -> setOf(int, long, float, double)
                char -> setOf(int, long, float, double)
                int -> setOf(long, float, double)
                long -> setOf(float, double)
                float -> setOf(double)
                boolean -> setOf()
                double -> setOf()
                void -> setOf()
                else -> setOf()
            }

    fun narrowingPrimitiveConversion(s: String): Set<String> =
            when (s) {
                void -> setOf()
                boolean -> setOf()
                byte -> setOf()
                short -> setOf(byte, char)
                char -> setOf(byte, short)
                int -> setOf(byte, short, char)
                long -> setOf(byte, short, char, int)
                float -> setOf(byte, short, char, int, long)
                double -> setOf(byte, short, char, int, long, float)
                else -> setOf()
            }


    // reference/boxed types

    const val refBoolean = "java.lang.Boolean"
    const val refByte = "java.lang.Byte"
    const val refChar = "java.lang.Character"
    const val refDouble = "java.lang.Double"
    const val refFloat = "java.lang.Float"
    const val refInt = "java.lang.Integer"
    const val refLong = "java.lang.Long"
    const val refShort = "java.lang.Short"
    const val refVoid = "java.lang.Void"

    val refPrimitives = setOf(refBoolean, refByte, refChar, refDouble, refFloat, refInt, refLong, refShort, refVoid)

    fun boxedType(s: String): String? =
            when (s) {
                boolean -> refBoolean
                byte -> refByte
                char -> refChar
                double -> refDouble
                float -> refFloat
                int -> refInt
                long -> refLong
                short -> refShort
                void -> refVoid
                else -> null
            }

    fun unboxedType(s: String, nonFullyQualified: Boolean = false): String? =
            when (s) {
                refBoolean -> boolean
                refByte -> byte
                refChar -> char
                refDouble -> double
                refFloat -> float
                refInt -> int
                refLong -> long
                refShort -> short
                refVoid -> void
                else -> if (nonFullyQualified) {
                    when (s) {
                        refBoolean.substringAfterLast(".") -> boolean
                        refByte.substringAfterLast(".") -> byte
                        refChar.substringAfterLast(".") -> char
                        refDouble.substringAfterLast(".") -> double
                        refFloat.substringAfterLast(".") -> float
                        refInt.substringAfterLast(".") -> int
                        refLong.substringAfterLast(".") -> long
                        refShort.substringAfterLast(".") -> short
                        refVoid.substringAfterLast(".") -> void
                        else -> null
                    }
                } else {
                    null
                }
            }
}
