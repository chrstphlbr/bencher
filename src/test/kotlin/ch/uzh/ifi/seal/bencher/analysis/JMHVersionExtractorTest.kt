package ch.uzh.ifi.seal.bencher.analysis

import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JMHVersionExtractorTest {

    @Test
    fun twoBenchs121() {
        val url = JarTestHelper.jar2BenchsJmh121.fileResource()

        val v = JMHVersionExtractor(url).getVersion()
        Assertions.assertTrue(v.isRight())
        v.map {
            Assertions.assertTrue(it == JarTestHelper.jar2BenchsJmh121Version)
        }
    }

    @Test
    fun fourBenchs121() {
        val url = JarTestHelper.jar4BenchsJmh121.fileResource()

        val v = JMHVersionExtractor(url).getVersion()
        Assertions.assertTrue(v.isRight())
        v.map {
            Assertions.assertTrue(it == JarTestHelper.jar4BenchsJmh121Version)
        }
    }

    @Test
    fun twoBenchs110() {
        val url = JarTestHelper.jar2BenchsJmh110.fileResource()

        val v = JMHVersionExtractor(url).getVersion()
        Assertions.assertTrue(v.isRight())
        v.map {
            Assertions.assertTrue(it == JarTestHelper.jar2BenchsJmh110Version)
        }
    }

    @Test
    fun fourBenchs110() {
        val url = JarTestHelper.jar4BenchsJmh110.fileResource()

        val v = JMHVersionExtractor(url).getVersion()
        Assertions.assertTrue(v.isRight())
        v.map {
            Assertions.assertTrue(it == JarTestHelper.jar4BenchsJmh110Version)
        }
    }

    @Test
    fun jmhVersionSpecified() {
        val url = JarTestHelper.jar4BenchsJmh110.fileResource()

        val v = JMHVersionExtractor(url)
        v.getVersion()
        Assertions.assertTrue(v.isVersionSpecified())
    }

    @Test
    fun noJmhVersionSpecified() {
        val url = JarTestHelper.jar4BenchsJmh10.fileResource()

        val v = JMHVersionExtractor(url)
        v.getVersion()
        Assertions.assertTrue(!v.isVersionSpecified())
    }
}