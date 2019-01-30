package ch.uzh.ifi.seal.bencher.analysis

enum class AccessModifier {
    PUBLIC, PACKAGE, PROTECTED, PRIVATE;

    companion object {
        val all: Set<AccessModifier> = setOf(PUBLIC, PACKAGE, PROTECTED, PRIVATE)
    }
}
