package ch.uzh.ifi.seal.bencher

data class JMHVersion(
        val major: Int,
        val minor: Int,
        val patch: Int = 0
) : Comparable<JMHVersion> {

    override fun compareTo(other: JMHVersion): Int =
            if (this == other) {
                0
            } else if (this.major < other.major) {
                -1
            } else if (this.major == other.major && this.minor < other.minor) {
                -1
            } else if (this.major == other.major && this.minor == other.minor && this.patch < other.patch) {
                -1
            } else {
                1
            }

    override fun toString(): String {
        return if (patch == 0) {
            "$major.$minor"
        } else {
            "$major.$minor.$patch"
        }
    }
}
