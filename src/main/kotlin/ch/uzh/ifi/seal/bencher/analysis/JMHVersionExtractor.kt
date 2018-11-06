package ch.uzh.ifi.seal.bencher.analysis

import ch.uzh.ifi.seal.bencher.JMHVersion
import org.funktionale.either.Either
import java.io.File
import java.nio.file.Files

class JMHVersionExtractor(private val jar: File, private val defaultJMHVersion: JMHVersion = JMHVersion(1, 20)) {

    private var version: JMHVersion? = null

    fun getVersion(): Either<String, JMHVersion> {
        val p = Files.createTempDirectory(tmpDirPrefix)
        val tmpDir = File(p.toUri())

        val ret = JarHelper.unzip(jar, "jmh.properties", tmpDir.toString())

        return if (ret.isLeft() && ret.left().get() == "File (jmh.properties) does not exist") {
            JarHelper.deleteTmpDir(tmpDir)
            return Either.right(defaultJMHVersion)
        } else if (ret.isLeft()) {
            JarHelper.deleteTmpDir(tmpDir)
            return Either.left(ret.left().get())
        } else {
            val propertiesFile = ret.right().get()

            if (propertiesFile.exists()) {
                propertiesFile.forEachLine {
                    if (it.startsWith("jmh.version=")) {
                        val v = it.split("=", limit = 2)[1].split(".", limit = 2)
                        version = JMHVersion(v[0].toInt(), v[1].toInt())
                        return@forEachLine
                    }
                }
            }

            JarHelper.deleteTmpDir(tmpDir)
            Either.right(version ?: defaultJMHVersion)
        }
    }

    fun isVersionSpecified(): Boolean {
        return version != null
    }

    companion object {
        private val tmpDirPrefix = "bencher-JMHVersionExtractor-"
    }
}