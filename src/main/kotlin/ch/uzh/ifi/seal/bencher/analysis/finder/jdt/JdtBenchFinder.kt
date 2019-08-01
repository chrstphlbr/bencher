package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchFinder
import ch.uzh.ifi.seal.bencher.fileResource
import ch.uzh.ifi.seal.bencher.replaceDotsWithSlashes
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.FileASTRequestor
import org.funktionale.either.Either
import java.io.File
import java.nio.file.Paths

class JdtBenchFinder(private val sourceDirectory: File, private val prefix: String = "") : BenchFinder() {

    override fun all(): Either<String, List<Benchmark>> {
        if (parsed) {
            return Either.right(benchs)
        }

        parsed = true
        benchs = benchs()
        return Either.right(benchs)
    }

    private fun benchs(): List<Benchmark> {
        val bcfs = mutableListOf<JdtBenchClassFinder>()

        val classPaths = arrayOf(getJmhJar())

        val filePaths = sourceDirectory.walkTopDown().filter { f ->
            f.isFile && f.extension == "java" && f.absolutePath.contains(prefix.replaceDotsWithSlashes)
        }.map { it.absolutePath }.toList().toTypedArray()

        val parser = ASTParser.newParser(AST.JLS11)
        parser.setKind(ASTParser.K_COMPILATION_UNIT)
        parser.setEnvironment(classPaths, arrayOf(sourceDirectory.absolutePath), arrayOf("UTF-8"), true)
        parser.setResolveBindings(true)
        parser.setBindingsRecovery(true)
        parser.createASTs(filePaths, null, arrayOf(), object : FileASTRequestor() {
            override fun acceptAST(sourceFilePath: String, javaUnit: CompilationUnit) {
                val bcf = JdtBenchClassFinder()
                javaUnit.accept(bcf)
                bcfs.add(bcf)
                bcf.benchs()
            }
        }, null)

        bcfs.map {
            it.benchClass()
        }.flatten().map {
            saveExecInfos(it.first, it.second)
        }

        return bcfs.map { it.benchs() }.flatten()
    }

    // TODO: not optimal to have jmh as a jar in the resource folder
    private fun getJmhJar(): String {
        val copiedFile = "jmh-core.jar.zip".fileResource()
        val newFile = Paths.get(copiedFile.absolutePath.replace(".zip", "")).toFile()

        if (!newFile.exists()) {
            copiedFile.copyTo(newFile)
        }

        return newFile.absolutePath
    }
}