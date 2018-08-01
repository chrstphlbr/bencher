package ch.uzh.ifi.seal.bencher.analysis

import org.apache.logging.log4j.LogManager
import org.funktionale.either.Either
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.zip.ZipInputStream

object JarHelper {

    val log = LogManager.getLogger(JarHelper::class.java.canonicalName)

    fun deleteTmpDir(tmpDir: File) {
        val deleted = tmpDir.deleteRecursively()
        if (!deleted) {
            log.warn("Could not delete tmp folder '${tmpDir.absolutePath}'")
        }
    }

    fun extractJar(tmpDir: File, jar: File, name: String): Either<String, File> {
        if (!jar.exists()) {
            return Either.left("Jar file (${jar.absolutePath}) does not exist")
        }
        if (jar.extension != "jar") {
            return Either.left("Jar file (${jar.absolutePath}) not a jar file (extension wrong, expected '.jar')")
        }
        val p = Paths.get(tmpDir.absolutePath, name)
        return unzip(jar, p.toString())
    }

    fun unzip(jar: File, to: String): Either<String, File> {
        val outDir = File(to)
        val outDirCreated = outDir.mkdirs()
        if (!outDirCreated) {
            return Either.left("Could not create outdir ($to)")
        }

        // create META-INF folder
        val metaInf = "META-INF"
        val metaInfCreated = Paths.get(to, metaInf).toFile().mkdir()
        if (!metaInfCreated) {
            return Either.left("Could not create META-INF folder in outdir ($to)")
        }

        val buf = ByteArray(1024)
        val zis = ZipInputStream(FileInputStream(jar))
        var ze = zis.nextEntry
        while (ze != null) {
            val fn = ze.name

            val p = Paths.get(to, fn)
            val f = p.toFile()

            if (ze.isDirectory) {
                if (!f.exists()) {
                    // try to create folder if not already existing
                    val dirCreated = f.mkdir()
                    if (!dirCreated) {
                        return Either.left("Could not create dir ($p)")
                    }
                }
            } else {
                val fos = FileOutputStream(f)
                fosloop@ while (true) {
                    val len = zis.read(buf)
                    if (len > 0) {
                        fos.write(buf, 0, len)
                    } else {
                        break@fosloop
                    }

                }
                fos.close()
            }

            ze = zis.nextEntry
        }

        return Either.right(outDir)
    }
}
