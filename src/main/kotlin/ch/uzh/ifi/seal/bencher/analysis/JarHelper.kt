package ch.uzh.ifi.seal.bencher.analysis

import arrow.core.Either
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.zip.ZipFile
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
            return Either.Left("Jar file (${jar.absolutePath}) does not exist")
        }
        if (jar.extension != "jar") {
            return Either.Left("Jar file (${jar.absolutePath}) not a jar file (extension wrong, expected '.jar')")
        }
        val p = Paths.get(tmpDir.absolutePath, name)
        return unzip(jar, p.toString())
    }

    fun unzip(jar: File, to: String): Either<String, File> {
        val outDir = File(to)
        val outDirCreated = outDir.exists() || outDir.mkdirs()
        if (!outDirCreated) {
            return Either.Left("Could not create outdir ($to)")
        }

        // create META-INF folder
        val metaInf = "META-INF"
        val metaInfDir = Paths.get(to, metaInf).toFile()
        val metaInfCreated = metaInfDir.exists() || metaInfDir.mkdir()
        if (!metaInfCreated) {
            return Either.Left("Could not create META-INF folder in outdir ($to)")
        }

        val buf = ByteArray(1024)

        ZipInputStream(FileInputStream(jar)).use { zis ->
            var ze = zis.nextEntry
            while (ze != null) {
                val fn = ze.name

                val p = Paths.get(to, fn)
                val f = p.toFile()

                if (!ze.isDirectory) {
                    val d = f.parentFile
                    if (!d.exists()) {
                        // try to create folder if not already existing
                        val dirCreated = d.mkdirs()
                        if (!dirCreated) {
                            return Either.Left("Could not create dir ($p)")
                        }
                    }

                    FileOutputStream(f).use { fos ->
                        fosloop@ while (true) {
                            val len = zis.read(buf)
                            if (len > 0) {
                                fos.write(buf, 0, len)
                            } else {
                                break@fosloop
                            }

                        }
                    }
                }

                ze = zis.nextEntry
            }
        }

        return Either.Right(outDir)
    }

    fun unzip(jar: File, file: String, to: String): Either<String, File> =
        unzip(jar, listOf(file), to).map { it.first() }

    fun unzip(jar: File, files: Iterable<String>, to: String): Either<String, List<File>> {
        val ret = mutableListOf<File>()

        ZipFile(jar).use { zf ->
            files.forEach { file ->
                val entry = zf.getEntry(file) ?: return Either.Left("File ($file) does not exist")

                zf.getInputStream(entry).use { stream ->
                    val tmpFile = Paths.get(to, file).toFile()

                    val dirCreated = tmpFile.parentFile.exists() || tmpFile.parentFile.mkdirs()
                    if (!dirCreated) {
                        return Either.Left("Could not create folder (${tmpFile.parentFile})")
                    }

                    val fileCreated = tmpFile.exists() || tmpFile.createNewFile()
                    if (!fileCreated) {
                        return Either.Left("Could not create file ($tmpFile)")
                    }

                    tmpFile.outputStream().use { outputStream -> stream.copyTo(outputStream) }
                    ret.add(tmpFile)
                }
            }
        }

        return Either.Right(ret)
    }
}