package ch.uzh.ifi.seal.bencher

import arrow.core.getOrElse
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class VersionTest {
    @Test
    fun compareEqualMajor() {
        val v1 = Version(major = 1)
        val v2 = Version(major = 1)
        val comp1 = v1.compareTo(v2)
        val comp2 = v2.compareTo(v1)

        Assertions.assertEquals(0, comp1)
        Assertions.assertEquals(0, comp2)
    }

    @Test
    fun compareEqualMajorMinor() {
        val v1 = Version(major = 1, minor = 2)
        val v2 = Version(major = 1, minor = 2)
        val comp1 = v1.compareTo(v2)
        val comp2 = v2.compareTo(v1)

        Assertions.assertEquals(0, comp1)
        Assertions.assertEquals(0, comp2)
    }

    @Test
    fun compareEqualMajorMinorPatch() {
        val v1 = Version(major = 1, minor = 2, patch = 3)
        val v2 = Version(major = 1, minor = 2, patch = 3)
        val comp1 = v1.compareTo(v2)
        val comp2 = v2.compareTo(v1)

        Assertions.assertEquals(0, comp1)
        Assertions.assertEquals(0, comp2)
    }

    @Test
    fun compareLowerMajor() {
        val v1 = Version(major = 1)
        val v2 = Version(major = 2)
        val comp1 = v1.compareTo(v2)
        val comp2 = v2.compareTo(v1)

        Assertions.assertTrue(comp1 < 0)
        Assertions.assertTrue(comp2 > 0)
    }

    @Test
    fun compareEqualMajorLowerMinor() {
        val v1 = Version(major = 1, minor = 1)
        val v2 = Version(major = 1, minor = 2)
        val comp1 = v1.compareTo(v2)
        val comp2 = v2.compareTo(v1)

        Assertions.assertTrue(comp1 < 0)
        Assertions.assertTrue(comp2 > 0)
    }

    @Test
    fun compareEqualMajorEqualMinorLowerPatch() {
        val v1 = Version(major = 1, minor = 2, patch = 2)
        val v2 = Version(major = 1, minor = 2, patch = 3)
        val comp1 = v1.compareTo(v2)
        val comp2 = v2.compareTo(v1)

        Assertions.assertTrue(comp1 < 0)
        Assertions.assertTrue(comp2 > 0)
    }

    @Test
    fun compareHigherMajor() {
        val v1 = Version(major = 2)
        val v2 = Version(major = 1)
        val comp1 = v1.compareTo(v2)
        val comp2 = v2.compareTo(v1)

        Assertions.assertTrue(comp1 > 0)
        Assertions.assertTrue(comp2 < 0)
    }

    @Test
    fun compareEqualMajorHigherMinor() {
        val v1 = Version(major = 1, minor = 2)
        val v2 = Version(major = 1, minor = 1)
        val comp1 = v1.compareTo(v2)
        val comp2 = v2.compareTo(v1)

        Assertions.assertTrue(comp1 > 0)
        Assertions.assertTrue(comp2 < 0)
    }

    @Test
    fun compareEqualMajorEqualMinorHigherPatch() {
        val v1 = Version(major = 1, minor = 2, patch = 3)
        val v2 = Version(major = 1, minor = 2, patch = 2)
        val comp1 = v1.compareTo(v2)
        val comp2 = v2.compareTo(v1)

        Assertions.assertTrue(comp1 > 0)
        Assertions.assertTrue(comp2 < 0)
    }

    @Test
    fun toMajor() {
        val major = 1
        val v1 = Version(major = major)
        val v1Str = Version.toString(v1)
        Assertions.assertEquals(major.toString(), v1Str)

        val v2 = Version(major = major, minor = null)
        val v2Str = Version.toString(v2)
        Assertions.assertEquals(v1Str, v2Str)

        val v3 = Version(major = major, minor = null, patch = null)
        val v3Str = Version.toString(v3)
        Assertions.assertEquals(v1Str, v3Str)
    }

    @Test
    fun toMajorMinor() {
        val major = 1
        val minor = 2
        val v1 = Version(major = major, minor = minor)
        val v1Str = Version.toString(v1)
        Assertions.assertEquals("$major.$minor", v1Str)

        val v2 = Version(major = major, minor = minor, patch = null)
        val v2Str = Version.toString(v2)
        Assertions.assertEquals(v1Str, v2Str)
    }

    @Test
    fun toMajorMinorPatch() {
        val major = 1
        val minor = 2
        val patch = 3
        val v = Version(major = major, minor = minor, patch = patch)
        val vStr = Version.toString(v)
        Assertions.assertEquals("$major.$minor.$patch", vStr)
    }

    @Test
    fun fromMajor() {
        val str = "1"
        val v = Version.from(str).getOrElse {
            Assertions.fail("unexpected error: $it")
        }
        Assertions.assertEquals(Version(major = 1), v)
        Assertions.assertEquals(Version(major = 1, minor = null), v)
        Assertions.assertEquals(Version(major = 1, minor = null, patch = null), v)
    }

    @Test
    fun fromMajorMinor() {
        val str = "1.2"
        val v = Version.from(str).getOrElse {
            Assertions.fail("unexpected error: $it")
        }
        Assertions.assertEquals(Version(major = 1, minor = 2), v)
        Assertions.assertEquals(Version(major = 1, minor = 2, patch = null), v)
    }

    @Test
    fun fromMajorMinorPatch() {
        val str = "1.2.3"
        val v = Version.from(str).getOrElse {
            Assertions.fail("unexpected error: $it")
        }
        Assertions.assertEquals(Version(major = 1, minor = 2, patch = 3), v)
    }

    @Test
    fun fromWithWhitespaces() {
        val str = "     1.2.3      "
        val v = Version.from(str).getOrElse {
            Assertions.fail("unexpected error: $it")
        }
        Assertions.assertEquals(Version(major = 1, minor = 2, patch = 3), v)
    }

    @Test
    fun fromInvalidEmpty() {
        val str = ""
        val v = Version.from(str)
        Assertions.assertTrue(v.isLeft())
    }

    @Test
    fun fromInvalidTooManyDots() {
        val str = "1.2.3."
        val v = Version.from(str)
        Assertions.assertTrue(v.isLeft())
    }

    @Test
    fun fromInvalidTooManyVersions() {
        val str = "1.2.3.4.5"
        val v = Version.from(str)
        Assertions.assertTrue(v.isLeft())
    }

    @Test
    fun fromInvalidMajorNotANumber() {
        val str = "A.2.3"
        val v = Version.from(str)
        Assertions.assertTrue(v.isLeft())
    }

    @Test
    fun fromInvalidMinorNotANumber() {
        val str = "1.B.3"
        val v = Version.from(str)
        Assertions.assertTrue(v.isLeft())
    }

    @Test
    fun fromInvalidPatchNotANumber() {
        val str = "1.2.C"
        val v = Version.from(str)
        Assertions.assertTrue(v.isLeft())
    }
}
