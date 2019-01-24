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

    val primitives = setOf(boolean, byte, char, double, float, int, long, short)

    const val refBoolean = "java.lang.Boolean"
    const val refByte = "java.lang.Byte"
    const val refChar = "java.lang.Character"
    const val refDouble = "java.lang.Double"
    const val refFloat = "java.lang.Float"
    const val refInt = "java.lang.Integer"
    const val refLong = "java.lang.Long"
    const val refShort = "java.lang.Short"
}
