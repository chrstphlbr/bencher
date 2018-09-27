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

private val String.baseType: String
    get() = if (this.isEmpty()) {
        ""
    } else {
        when (this[0]) {
            ByteCodeConstants.byte.first -> ByteCodeConstants.byte.second
            ByteCodeConstants.char.first -> ByteCodeConstants.char.second
            ByteCodeConstants.double.first -> ByteCodeConstants.double.second
            ByteCodeConstants.float.first -> ByteCodeConstants.float.second
            ByteCodeConstants.int.first -> ByteCodeConstants.int.second
            ByteCodeConstants.long.first -> ByteCodeConstants.long.second
            ByteCodeConstants.short.first -> ByteCodeConstants.short.second
            ByteCodeConstants.boolean.first -> ByteCodeConstants.boolean.second
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
