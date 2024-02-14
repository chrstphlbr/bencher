package ch.uzh.ifi.seal.bencher

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.time.Duration
import java.util.concurrent.TimeUnit

fun String.runCommand(
        workingDir: File,
        timeout: Duration,
        env: Map<String, String> = mapOf()
): Triple<Boolean, String?, String?> {
    try {
        val parts = this.split("\\s".toRegex())
        val procBuilder = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
        procBuilder.environment().putAll(env)

        val proc = procBuilder.start()

        // Read output and error streams in separate threads
        val output = StringBuilder()
        val error = StringBuilder()

        val outputThread = Thread {
            proc.inputStream.bufferedReader().use { output.append(it.readText()) }
        }

        val errorThread = Thread {
            proc.errorStream.bufferedReader().use { error.append(it.readText()) }
        }

        outputThread.start()
        errorThread.start()

        // true if normal exit, false if timed out
        val exited = proc.waitFor(timeout.seconds, TimeUnit.SECONDS)

        // Wait for the output and error reading threads to finish
        outputThread.join()
        errorThread.join()

        return Triple(exited, output.toString(), error.toString())
    } catch (e: Throwable) {
        e.printStackTrace()
        return Triple(false, null, e.message)
    }
}

fun String.fileResource(): File {
    val r = Thread.currentThread().contextClassLoader.getResource(this)

    return if (r.protocol == "jar") {
        val ef = r.toExternalForm()

        val tmp = Files.createTempDirectory("bencher-")
        val tmpFile = tmp.toFile()
        tmpFile.deleteOnExit()

        val path = ef.substringAfter("file:")
        val file = path.split("!/")

        JarHelper
            .unzip(File(file[0]), file[1], tmpFile.absolutePath.toString())
            .getOrElse { File(ef) }
    } else {
        File(r.toURI())
    }
}

val String.replaceDotsWithFileSeparator: String
    inline get() = this.replace(".", File.separator)

val String.replaceFileSeparatorWithDots: String
    inline get() = this.replace(File.separator, ".")

val String.replaceDotsWithSlashes: String
    inline get() = this.replace(".", "/")

val String.replaceSlashesWithDots: String
    inline get() = this.replace("/", ".")

val String.sha265: ByteArray
    inline get() {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(this.toByteArray())
        return messageDigest.digest()
    }
