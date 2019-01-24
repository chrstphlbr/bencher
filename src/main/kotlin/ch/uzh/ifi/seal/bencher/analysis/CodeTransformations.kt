package ch.uzh.ifi.seal.bencher.analysis

import ch.uzh.ifi.seal.bencher.replaceDotsWithSlashes
import ch.uzh.ifi.seal.bencher.replaceSlashesWithDots
import org.funktionale.option.Option

fun String.byteCode(trailingSemicolon: Boolean = false): String =
        when {
            this == "" -> ""
            this.endsWith("[]") -> "[${this.substring(0, this.length-2).byteCode(trailingSemicolon)}"
            else -> when (this) {
                SourceCodeConstants.byte -> ByteCodeConstants.byte.toString()
                SourceCodeConstants.char -> ByteCodeConstants.char.toString()
                SourceCodeConstants.double -> ByteCodeConstants.double.toString()
                SourceCodeConstants.float -> ByteCodeConstants.float.toString()
                SourceCodeConstants.int -> ByteCodeConstants.int.toString()
                SourceCodeConstants.long -> ByteCodeConstants.long.toString()
                SourceCodeConstants.short -> ByteCodeConstants.short.toString()
                SourceCodeConstants.boolean -> ByteCodeConstants.boolean.toString()
                else -> if (trailingSemicolon) {
                    "L${this.replaceDotsWithSlashes};"
                } else {
                    "L${this.replaceDotsWithSlashes}"
                }
            }

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
                    this.scBaseType
            }
        }
    }

fun isPrimitive(s: String): Boolean =
        ByteCodeConstants.primitives.map { it.toString() }.contains(s) || SourceCodeConstants.primitives.contains(s)

fun isBoxedPrimitive(s: String, nonFullyQualified: Boolean = false, trailingSemicolon: Boolean = false): Boolean =
        SourceCodeConstants.refPrimitives.flatMap {
            val l = mutableListOf<String>()
            l.add(it.byteCode(trailingSemicolon))
            l.add(it)
            if (nonFullyQualified) {
                l.add(it.substringAfter("."))
            }
            l
        }.contains(s)

private val String.scBaseType: String
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

private val String.bcBaseType: String
    get() = if (this.isEmpty()) {
        ""
    } else {
        when (this) {
            SourceCodeConstants.byte -> ByteCodeConstants.byte.toString()
            SourceCodeConstants.char -> ByteCodeConstants.char.toString()
            SourceCodeConstants.double -> ByteCodeConstants.double.toString()
            SourceCodeConstants.float -> ByteCodeConstants.float.toString()
            SourceCodeConstants.int -> ByteCodeConstants.int.toString()
            SourceCodeConstants.long -> ByteCodeConstants.long.toString()
            SourceCodeConstants.short -> ByteCodeConstants.short.toString()
            SourceCodeConstants.boolean -> ByteCodeConstants.boolean.toString()
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
