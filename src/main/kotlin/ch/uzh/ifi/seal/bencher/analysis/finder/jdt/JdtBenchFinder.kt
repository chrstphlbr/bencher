package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.StateObjectManager
import ch.uzh.ifi.seal.bencher.fileResource
import ch.uzh.ifi.seal.bencher.replaceDotsWithFileSeparator
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
        benchs()
        return Either.right(benchs)
    }

    private fun benchs() {
        val som = searchStateObjects()

        val filePaths = sourceDirectory.walkTopDown().filter { f ->
            f.isFile && f.extension == "java" && f.absolutePath.contains(prefix.replaceDotsWithFileSeparator)
        }.map { it.absolutePath }.toList().toTypedArray()

        val bcfs = mutableListOf<JdtBenchClassFinder>()
        parse(filePaths, bcfs) { javaUnit, bcfs ->
            val bcf = JdtBenchClassFinder { node, cvs ->
                val cv = JdtBenchClassVisitor(som)
                cv.visit(node)
                cvs.add(cv)
            }
            javaUnit.accept(bcf)
            bcfs.add(bcf)
            bcf.benchs()
        }

        bcfs.map {
            it.benchClass()
        }.flatten().forEach {
            saveExecInfos(it.first, it.second)
        }
    }

    private fun searchStateObjects(): StateObjectManager {
        val som = StateObjectManager()

        val filePaths = sourceDirectory.walkTopDown().filter { f ->
            f.isFile && f.extension == "java"
        }.map { it.absolutePath }.toList().toTypedArray()

        val bcfs = mutableListOf<JdtBenchClassFinder>()
        parse(filePaths, bcfs) { javaUnit, _ ->
            val bcf = JdtBenchClassFinder { node, _ ->
                val cv = JdtBenchStateObjectVisitor(som)
                cv.visit(node)
            }
            javaUnit.accept(bcf)
        }

        return som
    }

    private fun getJmhJar(): String {
        val copiedFile = "jmh-core.jar.zip".fileResource()
        val newFile = Paths.get(copiedFile.absolutePath.replace(".zip", "")).toFile()

        if (!newFile.exists()) {
            copiedFile.copyTo(newFile)
        }

        return newFile.absolutePath
    }

    private fun parse(filePaths: Array<String>, bcfs: MutableList<JdtBenchClassFinder>, action: (javaUnit: CompilationUnit, bcfs: MutableList<JdtBenchClassFinder>) -> Unit) {
        val classPaths = arrayOf(getJmhJar())

        val parser = ASTParser.newParser(AST.JLS11)
        parser.setKind(ASTParser.K_COMPILATION_UNIT)
        parser.setEnvironment(classPaths, arrayOf(sourceDirectory.absolutePath), arrayOf("UTF-8"), true)
        parser.setResolveBindings(true)
        parser.setBindingsRecovery(true)
        parser.createASTs(filePaths, null, arrayOf(), object : FileASTRequestor() {
            override fun acceptAST(sourceFilePath: String, javaUnit: CompilationUnit) {
                action(javaUnit, bcfs)
            }
        }, null)
    }
}