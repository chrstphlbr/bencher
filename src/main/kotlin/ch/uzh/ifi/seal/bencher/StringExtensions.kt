package ch.uzh.ifi.seal.bencher

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

        // true if normal exit, false if timed out
        val exited = proc.waitFor(timeout.seconds, TimeUnit.SECONDS)
        return Triple(exited, proc.inputStream.bufferedReader().readText(), proc.errorStream.bufferedReader().readText())
    } catch(e: Throwable) {
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

        val ewef = JarHelper.unzip(File(file[0]), file[1], tmpFile.absolutePath.toString())
        if (ewef.isLeft()) {
            File(ef)
        } else {
            ewef.right().get()
        }
    } else {
        File(r.toURI())
    }
}

val String.replaceDotsWithSlashes: String
    inline get() = this.replace(".", File.separator)

val String.replaceSlashesWithDots: String
    inline get() = this.replace(File.separator, ".")

val String.sha265: ByteArray
    inline get() {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(this.toByteArray())
        return messageDigest.digest()
    }
