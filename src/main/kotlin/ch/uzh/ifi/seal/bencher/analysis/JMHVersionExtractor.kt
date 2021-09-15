package ch.uzh.ifi.seal.bencher.analysis

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.JMHVersion
import java.io.File
import java.nio.file.Files

class JMHVersionExtractor(private val jar: File, private val defaultJMHVersion: JMHVersion = JMHVersion(1, 20)) {

    private var version: JMHVersion? = null

    fun getVersion(): Either<String, JMHVersion> {
        val p = Files.createTempDirectory(tmpDirPrefix)
        val tmpDir = File(p.toUri())

        val ret = JarHelper.unzip(jar, "jmh.properties", tmpDir.toString())

        try {
            return ret
                .mapLeft {
                    if (it == "File (jmh.properties) does not exist") {
                        return Either.Right(defaultJMHVersion)
                    }
                    it
                }.map { propertiesFile ->
                    if (propertiesFile.exists()) {
                        propertiesFile.forEachLine {
                            if (it.startsWith("jmh.version=")) {
                                val v = it.split("=", limit = 2)[1].split(".", limit = 2)
                                version = JMHVersion(v[0].toInt(), v[1].toInt())
                                return@forEachLine
                            }
                        }
                    }

                    version ?: defaultJMHVersion
                }
        } finally {
            JarHelper.deleteTmpDir(tmpDir)
        }
    }

    fun isVersionSpecified(): Boolean {
        return version != null
    }

    companion object {
        private val tmpDirPrefix = "bencher-JMHVersionExtractor-"
    }
}