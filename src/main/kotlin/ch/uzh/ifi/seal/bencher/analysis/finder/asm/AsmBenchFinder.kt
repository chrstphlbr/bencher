package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.StateObjectManager
import ch.uzh.ifi.seal.bencher.replaceDotsWithFileSeparator
import ch.uzh.ifi.seal.bencher.replaceFileSeparatorWithDots
import org.funktionale.either.Either
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths

class AsmBenchFinder(private val jar: File, pkgPrefix: String = "") : BenchFinder() {

    private val pathPrefix: String = pkgPrefix.replaceDotsWithFileSeparator

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

            parsed = true
            benchs(eJarFileFolder.right().get())
            return Either.right(benchs)
        } finally {
            JarHelper.deleteTmpDir(tmpDir)
        }
    }

    private fun benchs(jarDir: File) {
        val som = searchStateObjects(jarDir)

        jarDir.walkTopDown().filter { f ->
            f.isFile && f.extension == "class" && f.absolutePath.startsWith(Paths.get(jarDir.absolutePath, pathPrefix).toString())
        }.forEach { f ->
            val cr = ClassReader(FileInputStream(f))
            val opcode = Opcodes.ASM7
            val className = convertClassName(f, jarDir)

            val cv = AsmBenchClassVisitor(
                    api = opcode,
                    cv = null,
                    className = className,
                    som = som
            )
            cr.accept(cv, opcode)

            saveExecInfos(className, cv.benchClass)
        }
    }

    private fun searchStateObjects(jarDir: File): StateObjectManager {
        val som = StateObjectManager()

        jarDir.walkTopDown().filter { f ->
            f.isFile && f.extension == "class"
        }.forEach { f ->
            val cr = ClassReader(FileInputStream(f))
            val opcode = Opcodes.ASM7
            val className = convertClassName(f, jarDir)

            val test = AsmBenchStateObjectVisitor(api = opcode,
                    cv = null,
                    className = className, som = som)
            cr.accept(test, opcode)
        }

        return som
    }

    // replace absolute path such as /Users/user/projectdir/src/main/java/pkg1/pkg2/ClassName.class to pkg1.pkg2.ClassName
    private fun convertClassName(f: File, jarDir: File) = f.absolutePath
            .substringAfter(jarDir.absolutePath)
            .substringAfter(File.separator)
            .replace(".class", "")
            .replaceFileSeparatorWithDots

    companion object {
        private const val tmpDirPrefix = "bencher-AsmBenchFinder-"
        private const val jarExtractionDir = "jar"
    }
}
