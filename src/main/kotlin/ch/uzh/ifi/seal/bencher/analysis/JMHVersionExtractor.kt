package ch.uzh.ifi.seal.bencher.analysis

import ch.uzh.ifi.seal.bencher.JMHVersion
import ch.uzh.ifi.seal.bencher.runCommand
import org.funktionale.either.Either
import java.io.File
import java.nio.file.Files
import java.time.Duration

class JMHVersionExtractor(private val jar: File) {

    private val defaultJMHVersion = JMHVersion(1, 20)
    private val defaultTimeout = Duration.ofMinutes(1)
    private var version: JMHVersion? = null

    fun getVersion(): Either<String, JMHVersion> {
        val p = Files.createTempDirectory(tmpDirPrefix)
        val tmpDir = File(p.toUri())

        val cmd = "jar -xf $jar jmh.properties"
        val ret = cmd.runCommand(tmpDir, defaultTimeout)

        if (!ret.first) {
            return Either.left("Error during extracting jmh.properties file")
        }

        val propertiesFile = File("$tmpDir/jmh.properties")

        if (propertiesFile.exists()) {
            propertiesFile.forEachLine {
                if (it.startsWith("jmh.version=")) {
                    val v = it.split("=", limit = 2)[1].split(".", limit = 2)
                    version = JMHVersion(v[0].toInt(), v[1].toInt())
                    return@forEachLine
                }
            }
        }

        return Either.right(version ?: defaultJMHVersion)
    }

    fun isVersionSpecified(): Boolean {
        return version != null
    }

    companion object {
        private val tmpDirPrefix = "bencher-JMHVersionExtractor-"
    }
}