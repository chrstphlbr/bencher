package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import ch.uzh.ifi.seal.bencher.replaceDotsWithSlashes
import ch.uzh.ifi.seal.bencher.replaceSlashesWithDots
import org.funktionale.either.Either
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths

class AsmBenchFinder(private val jar: String, pkgPrefix: String = "") : MethodFinder<Benchmark> {
    private val pathPrefix: String = pkgPrefix.replaceDotsWithSlashes

    private var parsed: Boolean = false
    private lateinit var benchs: List<Benchmark>

    override fun all(): Either<String, List<Benchmark>> {
        if (parsed) {
            return Either.right(benchs)
        }

        val p = Files.createTempDirectory(tmpDirPrefix)
        val tmpDir = File(p.toUri())

        try {
            val eJarFileFolder = JarHelper.extractJar(tmpDir, jar, jarExtractionDir)
            if (eJarFileFolder.isLeft()) {
                return Either.left("Could not extract jar file ($jar) into tmp folder ($p): ${eJarFileFolder.left().get()}")
            }

            benchs = benchs(eJarFileFolder.right().get())
            return Either.right(benchs)
        } finally {
            JarHelper.deleteTmpDir(tmpDir)
        }
    }

    private fun benchs(jarDir: File): List<Benchmark> =
            jarDir.walkTopDown().filter { f ->
                f.isFile && f.extension == "class" && f.absolutePath.startsWith(Paths.get(jarDir.absolutePath, pathPrefix).toString())
            }.map { f ->
                val cr = ClassReader(FileInputStream(f))
                val opcode = Opcodes.ASM6
                val cv = AsmBenchClassVisitor(
                        api = opcode,
                        cv = null,
                        // replace absolute path such as /Users/user/projectdir/src/main/java/pkg1/pkg2/ClassName.class to pkg1.pkg2.ClassName
                        className = f.absolutePath.replace(".class", "").substring(f.absolutePath.indexOf(pathPrefix)).replaceSlashesWithDots
                )
                cr.accept(cv, opcode)
                cv.benchs()
            }.flatten().toList()

    companion object {
        private val tmpDirPrefix = "bencher-AsmBenchFinder-"
        private val jarExtractionDir = "jar"
    }
}
