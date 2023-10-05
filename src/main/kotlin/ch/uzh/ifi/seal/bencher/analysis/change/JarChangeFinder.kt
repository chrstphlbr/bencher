package ch.uzh.ifi.seal.bencher.analysis.change

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchClassVisitor
import ch.uzh.ifi.seal.bencher.replaceDotsWithFileSeparator
import ch.uzh.ifi.seal.bencher.replaceFileSeparatorWithDots
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths

class JarChangeFinder(
        // Java package notation. E.g., org.sample
        pkgPrefixes: Set<String> = setOf(""),
        private val deleteTmpDir: Boolean = true
) : ChangeFinder {

    private val pathPrefixes = prefixes(pkgPrefixes)

    override fun changes(oldJar: File, newJar: File): Either<String, Set<Change>> {
        val p = Files.createTempDirectory(tmpDirPrefix(oldJar, newJar))
        val tmpDir = File(p.toUri())

        try {
            val j1 = JarHelper.extractJar(tmpDir, oldJar, "old")
                .getOrElse {
                    return Either.Left(it)
                }
            val j1Hashes = hashes(j1)

            val j2 = JarHelper.extractJar(tmpDir, newJar, "new")
                .getOrElse {
                    return Either.Left(it)
                }
            val j2Hashes = hashes(j2)

            val changes = jarChanges(j1Hashes, j2Hashes)

            return Either.Right(changes)
        } finally {
            if (deleteTmpDir) {
                JarHelper.deleteTmpDir(tmpDir)
            }
        }
    }

    private fun hashes(jarDir: File): Map<String, Map<Change, ByteArray>> =
            jarDir.walkTopDown().filter { f ->
                f.isFile && f.extension == "class" && fileInPackage(pathPrefixes, jarDir, f.absolutePath)
            }.associate { f ->
                val pathPrefix = pathPrefixes
                        .filter { pathPrefix ->
                            f.absolutePath.substringAfter(pathPrefix, prefixNotFound) != prefixNotFound
                        }
                        .maxByOrNull { it.length }
                        ?: throw IllegalStateException("should have at least one pathPrefix")

                val classPath = Paths.get(pathPrefix, f.absolutePath.substringAfter(pathPrefix)).toString()
                val className = classPath.replace(".class", "").replaceFileSeparatorWithDots
                Pair(className, fileHashes(f, className))
            }

    private fun prefixes(pkgPrefixes: Set<String>): Set<String> =
            pkgPrefixes
                    .map { it.replaceDotsWithFileSeparator }
                    .toSet()

    private fun fileInPackage(prefixes: Set<String>, jarDir: File, absoluteFilePath: String): Boolean =
            prefixes
                    .map { absoluteFilePath.startsWith(Paths.get(jarDir.absolutePath, it).toString()) }
                    .fold(false) { acc, b -> acc || b }


    private fun fileHashes(f: File, className: String): Map<Change, ByteArray> {
        FileInputStream(f).use {
            val cr = ClassReader(it)
            val opcode = Opcodes.ASM7
            val benchVisitor = AsmBenchClassVisitor(
                api = opcode,
                cv = null,
                className = className
            )
            val changeVisitor = AsmChangeClassVisitor(opcode, benchVisitor, "")
            cr.accept(changeVisitor, opcode)
            return changeVisitor.changes()
        }
    }

    private fun jarChanges(j1: Map<String, Map<Change, ByteArray>>, j2: Map<String, Map<Change, ByteArray>>): Set<Change> {
        val classesInBoth = j1.keys.intersect(j2.keys)
        val deletions = j1.filter { !j2.keys.contains(it.key) }.map { DeletionChange(ClassHeaderChange(clazz = Class(name = it.key))) }
        val additions = j2.filter { !j1.keys.contains(it.key) }.map { AdditionChange(ClassHeaderChange(clazz = Class(name = it.key))) }


        val changes: Set<Change> = classesInBoth.flatMap map@{ className ->
            val c1 = j1[className] ?: return@map emptySet<Change>()
            val c2 = j2[className] ?: return@map emptySet<Change>()
            classChanges(c1, c2)
        }.toSet()

        val ret = mutableSetOf<Change>()
        ret.addAll(changes)
        ret.addAll(deletions)
        ret.addAll(additions)
        return ret
    }

    private fun classChanges(c1: Map<Change, ByteArray>, c2: Map<Change, ByteArray>): Set<Change> {
        val changeKeys = c1.keys.intersect(c2.keys)
        val deletions = c1.filter { !changeKeys.contains(it.key) }.map { DeletionChange(it.key) }
        val additions = c2.filter { !changeKeys.contains(it.key) }.map { AdditionChange(it.key) }

        val changes: List<Change> = changeKeys.filter map@{ c ->
            val h1 = c1[c] ?: return@map false
            val h2 = c2[c] ?: return@map false
            !h1.contentEquals(h2)
        }

        val ret = mutableSetOf<Change>()
        ret.addAll(changes)
        ret.addAll(deletions)
        ret.addAll(additions)
        return ret
    }

    companion object {
        val log = LogManager.getLogger(JarChangeFinder::class.java.canonicalName)
        val tmpDirPrefix = "bencher"

        private const val prefixNotFound = "!!!"

        private fun tmpDirPrefix(oldJar: File, newJar: File): String =
                "$tmpDirPrefix-${jarDirName(oldJar)}-${jarDirName(newJar)}-"

        private fun jarDirName(jar: File): String =
                jar.absolutePath.substringAfterLast("/").replace(".", "_")
    }
}
