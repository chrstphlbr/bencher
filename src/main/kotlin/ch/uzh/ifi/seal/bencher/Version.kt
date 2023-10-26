package ch.uzh.ifi.seal.bencher

import arrow.core.Either

data class VersionPair(
    val v1: Version,
    val v2: Version,
) : Comparable<VersionPair> {
    init {
        if (v1 >= v2) {
            throw IllegalArgumentException("v1 must be smaller than v2")
        }
    }

    override fun compareTo(other: VersionPair): Int {
        val v1Comp = v1.compareTo(other.v1)
        if (v1Comp != 0) {
            return v1Comp
        }
        return v2.compareTo(other.v2)
    }
}

data class Version(
    val major: Int,
    val minor: Int? = null,
    val patch: Int? = null
) : Comparable<Version> {
    override fun compareTo(other: Version): Int {
        val thisMinor = minor ?: 0
        val thisPatch = patch ?: 0
        val otherMinor = other.minor ?: 0
        val otherPatch = other.patch ?: 0

        return when {
            major < other.major -> -1
            major > other.major -> 1
            thisMinor < otherMinor -> -1
            thisMinor > otherMinor -> 1
            thisPatch < otherPatch -> -1
            thisPatch > otherPatch -> 1
            else -> 0
        }
    }

    companion object {
        private const val DEL = "."

        fun toString(v: Version): String {
            val l = mutableListOf(v.major)
            if (v.minor != null) {
                l.add(v.minor)
            }
            if (v.patch != null) {
                l.add(v.patch)
            }
            return l.joinToString(DEL)
        }

        fun from(str: String): Either<String, Version> {
            val trimmed = str.trim()
            if (trimmed.isEmpty()) {
                return Either.Left("empty version string")
            }

            val splitted = trimmed
                .split(DEL, limit = 3)
                .mapIndexed { i, s ->
                    try {
                        s.toInt()
                    } catch (e: NumberFormatException) {
                        return Either.Left("could not parse version '$trimmed' at position $i: ${e.message}")
                    }
                }

            val v = when (splitted.size) {
                1 -> Version(major = splitted[0])
                2 -> Version(major = splitted[0], minor = splitted[1])
                3 -> Version(major = splitted[0], minor = splitted[1], patch = splitted[2])
                else -> null
            }

            return if (v != null) {
                Either.Right(v)
            } else {
                Either.Left("version string split in too many substrings: expected 3, was ${splitted.size}")
            }
        }
    }
}
