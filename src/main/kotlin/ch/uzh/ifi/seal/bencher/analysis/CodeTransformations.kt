package ch.uzh.ifi.seal.bencher.analysis

import ch.uzh.ifi.seal.bencher.replaceDotsWithSlashes
import ch.uzh.ifi.seal.bencher.replaceSlashesWithDots
import org.funktionale.option.Option

val String.byteCode: String
        get() = if (this.startsWith("L")) {
            this
        } else {
            "L${this.replaceDotsWithSlashes}"
        }

val String.sourceCode: String
    get() = when (this) {
        "" -> ""
        else -> Pair(this[0], this.substring(1)).let { (first, rest) ->
            when (first) {
                ByteCodeConstants.objectType -> // ObjectType
                rest.replaceSlashesWithDots.let { str ->
                    if (str[str.length - 1] == ';') {
                        str.substring(0, str.length - 1)
                    } else {
                        str
                    }
                }
                ByteCodeConstants.arrayType -> // ArrayType
                    "${rest.sourceCode}[]"
                else -> // BaseType
                    this.baseType
            }
        }
    }

fun isPrimitive(s: String): Boolean =
        ByteCodeConstants.primitives.map { it.toString() }.contains(s) || SourceCodeConstants.primitives.contains(s)

private val String.baseType: String
    get() = if (this.isEmpty()) {
        ""
    } else {
        when (this[0]) {
            ByteCodeConstants.byte -> SourceCodeConstants.byte
            ByteCodeConstants.char -> SourceCodeConstants.char
            ByteCodeConstants.double -> SourceCodeConstants.double
            ByteCodeConstants.float -> SourceCodeConstants.float
            ByteCodeConstants.int -> SourceCodeConstants.int
            ByteCodeConstants.long -> SourceCodeConstants.long
            ByteCodeConstants.short -> SourceCodeConstants.short
            ByteCodeConstants.boolean -> SourceCodeConstants.boolean
            else -> this
        }
    }

fun descriptorToParamList(desc: String): Option<List<String>> {
    if (desc.length == 0) {
        return Option.empty()
    }

    if (desc[0] != '(') {
        return Option.empty()
    }

    val paramEnd = desc.indexOf(')')
    if (paramEnd < 1) {
        return Option.empty()
    }

    return Option.Some(desc.substring(1, paramEnd).split(";").filter { !it.isBlank() }.map { it.sourceCode })
}

fun boxedType(s: String): String? =
        when (s) {
            SourceCodeConstants.boolean -> SourceCodeConstants.refBoolean
            SourceCodeConstants.byte -> SourceCodeConstants.refByte
            SourceCodeConstants.char -> SourceCodeConstants.refChar
            SourceCodeConstants.double -> SourceCodeConstants.refDouble
            SourceCodeConstants.float -> SourceCodeConstants.refFloat
            SourceCodeConstants.int -> SourceCodeConstants.refInt
            SourceCodeConstants.long -> SourceCodeConstants.refLong
            SourceCodeConstants.short -> SourceCodeConstants.refShort
            else -> null
        }

fun unboxedType(s: String, nonFullyQualified: Boolean = false): String? =
        when (s) {
            SourceCodeConstants.refBoolean -> SourceCodeConstants.boolean
            SourceCodeConstants.refByte -> SourceCodeConstants.byte
            SourceCodeConstants.refChar -> SourceCodeConstants.char
            SourceCodeConstants.refDouble -> SourceCodeConstants.double
            SourceCodeConstants.refFloat -> SourceCodeConstants.float
            SourceCodeConstants.refInt -> SourceCodeConstants.int
            SourceCodeConstants.refLong -> SourceCodeConstants.long
            SourceCodeConstants.refShort -> SourceCodeConstants.short
            else -> if (nonFullyQualified) {
                when (s) {
                    SourceCodeConstants.refBoolean.substringAfterLast(".") -> SourceCodeConstants.boolean
                    SourceCodeConstants.refByte.substringAfterLast(".") -> SourceCodeConstants.byte
                    SourceCodeConstants.refChar.substringAfterLast(".") -> SourceCodeConstants.char
                    SourceCodeConstants.refDouble.substringAfterLast(".") -> SourceCodeConstants.double
                    SourceCodeConstants.refFloat.substringAfterLast(".") -> SourceCodeConstants.float
                    SourceCodeConstants.refInt.substringAfterLast(".") -> SourceCodeConstants.int
                    SourceCodeConstants.refLong.substringAfterLast(".") -> SourceCodeConstants.long
                    SourceCodeConstants.refShort.substringAfterLast(".") -> SourceCodeConstants.short
                    else -> null
                }
            } else {
                null
            }
        }
