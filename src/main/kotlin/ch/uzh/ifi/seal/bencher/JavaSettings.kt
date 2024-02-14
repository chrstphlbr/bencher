package ch.uzh.ifi.seal.bencher

data class JavaSettings(
    val home: String?,
    val jvmArgs: String?,
) {
    fun homePair(): Pair<String, String>? =
        home?.let { Pair(JAVA_HOME, it) }

    companion object {
        const val JAVA_HOME = "JAVA_HOME"
    }
}
