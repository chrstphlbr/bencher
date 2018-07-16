package ch.uzh.ifi.seal.bencher.analysis

val String.byteCode: String
        get() = if (this.startsWith("L")) {
            this
        } else {
            "L${this.replace(".", "/")}"
        }

val String.sourceCode: String
    get() = if (this.startsWith("L")) {
        this.substring(1).replace("/", ".")
    } else {
        this
    }