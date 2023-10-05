package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.AbstractBenchmarkFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.StateObjectManager
import ch.uzh.ifi.seal.bencher.replaceDotsWithFileSeparator
import ch.uzh.ifi.seal.bencher.replaceFileSeparatorWithDots
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths

class AsmBenchFinder(
    private val jar: File,
    private val pkgPrefixes: Set<String> = setOf(""),
) : AbstractBenchmarkFinder() {

    private val opcode = Opcodes.ASM9

    override fun all(): Either<String, List<Benchmark>> {
        if (parsed) {
            return Either.Right(benchs)
        }

        val p = Files.createTempDirectory(tmpDirPrefix)
        val tmpDir = File(p.toUri())

        try {
            val jarFileFolder = JarHelper.extractJar(tmpDir, jar, jarExtractionDir)
                .getOrElse {
                    return Either.Left("Could not extract jar file ($jar) into tmp folder ($p): $it")
                }

            parsed = true
            benchs(jarFileFolder)
            return Either.Right(benchs)
        } finally {
            JarHelper.deleteTmpDir(tmpDir)
        }
    }

    private fun benchs(jarDir: File) {
        searchStateObjects(jarDir)

        val prefixes = prefixes(pkgPrefixes, jarDir)

        jarDir.walkTopDown().filter { f ->
            f.isFile && f.extension == "class" && fileWithPrefix(prefixes, f.absolutePath)
        }.forEach { f ->
            FileInputStream(f).use {
                val cr = ClassReader(it)
                val opcode = opcode
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
    }

    private fun prefixes(pkgPrefixes: Set<String>, jarDir: File): Set<String> =
            pkgPrefixes
                    .map { Paths.get(jarDir.absolutePath, it.replaceDotsWithFileSeparator).toString() }
                    .toSet()

    private fun fileWithPrefix(prefixes: Set<String>, absoluteFilePath: String): Boolean =
            prefixes
                    .map { absoluteFilePath.startsWith(it) }
                    .fold(false) { acc, b -> acc || b }

    private fun searchStateObjects(jarDir: File) {
        som = StateObjectManager()

        jarDir.walkTopDown().filter { f ->
            f.isFile && f.extension == "class"
        }.forEach { f ->
            FileInputStream(f).use {
                val cr = ClassReader(it)
                val opcode = opcode
                val className = convertClassName(f, jarDir)

                val test = AsmBenchStateObjectVisitor(api = opcode,
                    cv = null,
                    className = className, som = som)
                cr.accept(test, opcode)
            }
        }
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
