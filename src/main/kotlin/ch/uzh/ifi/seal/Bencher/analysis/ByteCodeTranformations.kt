package ch.uzh.ifi.seal.bencher.analysis

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

private val String.replaceDotsWithSlashes: String
        inline get() = this.replace(".", "/")

private val String.replaceSlashesWithDots: String
    inline get() = this.replace("/", ".")
