package ch.uzh.ifi.seal.bencher

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class VersionPairTest {

    @Test
    fun v1BeforeV2() {
        Assertions.assertDoesNotThrow { VersionPair(v1(), v2()) }
    }

    @Test
    fun v1EqualV2() {
        Assertions.assertThrows(IllegalArgumentException::class.java) { VersionPair(v1(), v1()) }
    }

    @Test
    fun v1AfterV2() {
        Assertions.assertThrows(IllegalArgumentException::class.java) { VersionPair(v2(), v1()) }
    }

    @Test
    fun equal() {
        val vp1 = VersionPair(v1(), v2())

        val vp2 = VersionPair(v1(), v2())

        Assertions.assertTrue(vp1 == vp2)
    }

    @Test
    fun vp1BeforeVp2() {
        val vp1 = VersionPair(v1(), v2())

        val vp2 = VersionPair(v2(), v3())

        Assertions.assertTrue(vp1 < vp2)
    }

    @Test
    fun vp1AfterVp2() {
        val vp1 = VersionPair(v2(), v3())

        val vp2 = VersionPair(v1(), v2())

        Assertions.assertTrue(vp2 < vp1)
    }

    @Test
    fun vp1EnclosingVp2() {
        val vp1 = VersionPair(v1(),v4())

        val vp2 = VersionPair(v2(), v3())

        Assertions.assertTrue(vp1 < vp2)
    }

    @Test
    fun vp1EnclosedByVp2() {
        val vp1 = VersionPair(v2(), v3())

        val vp2 = VersionPair(v1(),v4())

        Assertions.assertTrue(vp2 < vp1)
    }

    @Test
    fun vp1OverlappingVp2() {
        val vp1 = VersionPair(v1(), v3())

        val vp2 = VersionPair(v2(), v4())

        Assertions.assertTrue(vp1 < vp2)
    }

    @Test
    fun vp2OverlappingVp1() {
        val vp1 = VersionPair(v2(), v4())

        val vp2 = VersionPair(v1(), v3())

        Assertions.assertTrue(vp2 < vp1)
    }

    companion object {
        private val v1 = Version(major = 1, minor = 1, patch = 1)
        private val v2 = Version(major = 1, minor = 1, patch = 2)
        private val v3 = Version(major = 1, minor = 1, patch = 3)
        private val v4 = Version(major = 1, minor = 1, patch = 4)

        fun v1(): Version = v1.copy()

        fun v2(): Version = v2.copy()

        fun v3(): Version = v3.copy()

        fun v4(): Version = v4.copy()
    }
}