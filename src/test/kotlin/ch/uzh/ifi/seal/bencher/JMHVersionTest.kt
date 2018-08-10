package ch.uzh.ifi.seal.bencher

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JMHVersionTest {

    @Test
    fun equal() {
        val v1 = JMHVersion(major = 0, minor = 0)
        val v2 = JMHVersion(major = 0, minor = 0)
        Assertions.assertTrue(v1.compareTo(v2) == 0)
    }

    @Test
    fun lowerMajorLowerMinor() {
        val v1 = JMHVersion(major = 1, minor = 1)
        val v2 = JMHVersion(major = 2, minor = 2)
        Assertions.assertTrue(v1.compareTo(v2) == -1)
    }

    @Test
    fun lowerMajorEqualMinor() {
        val v1 = JMHVersion(major = 1, minor = 1)
        val v2 = JMHVersion(major = 2, minor = 1)
        Assertions.assertTrue(v1.compareTo(v2) == -1)
    }

    @Test
    fun lowerMajorHigherMinor() {
        val v1 = JMHVersion(major = 1, minor = 2)
        val v2 = JMHVersion(major = 2, minor = 1)
        Assertions.assertTrue(v1.compareTo(v2) == -1)
    }

    @Test
    fun higherMajorLowerMinor() {
        val v1 = JMHVersion(major = 2, minor = 1)
        val v2 = JMHVersion(major = 1, minor = 2)
        Assertions.assertTrue(v1.compareTo(v2) == 1)
    }

    @Test
    fun higherMajorEqualMinor() {
        val v1 = JMHVersion(major = 2, minor = 1)
        val v2 = JMHVersion(major = 1, minor = 1)
        Assertions.assertTrue(v1.compareTo(v2) == 1)
    }

    @Test
    fun higherMajorHigherMinor() {
        val v1 = JMHVersion(major = 2, minor = 2)
        val v2 = JMHVersion(major = 1, minor = 1)
        Assertions.assertTrue(v1.compareTo(v2) == 1)
    }
}
