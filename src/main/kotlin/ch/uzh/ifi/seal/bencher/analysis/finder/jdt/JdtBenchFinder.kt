package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.right
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.finder.MethodMetaInfos
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.AbstractBenchmarkFinder
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.StateObjectManager
import ch.uzh.ifi.seal.bencher.replaceDotsWithFileSeparator
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.FileASTRequestor
import java.io.File

class JdtBenchFinder(
        private val sourceDirectory: File,
        private val pkgPrefixes: Set<String> = setOf("")
) : AbstractBenchmarkFinder(), MethodMetaInfos {
    private val bcfs = mutableListOf<JdtBenchClassFinder>()

    override fun all(): Either<String, List<Benchmark>> {
        if (parsed) {
            return Either.Right(benchs)
        }

        parsed = true
        benchs()
        return Either.Right(benchs)
    }

    private fun benchs() {
        searchStateObjects()

        val filePaths = sourceDirectory.walkTopDown().filter { f ->
            f.isFile &&
                    f.extension == "java" &&
                    pkgPrefixes.fold(false) { acc, prefix ->
                        acc || f.absolutePath.contains(prefix.replaceDotsWithFileSeparator)
                    }
        }.map { it.absolutePath }.toList().toTypedArray()

        parse(filePaths, bcfs) { javaUnit, bcfs ->
            val bcf = JdtBenchClassFinder { node, cvs ->
                val cv = JdtBenchClassVisitor(som)
                node.accept(cv)
                cvs.add(cv)
            }
            javaUnit.accept(bcf)
            bcfs.add(bcf)
            bcf.benchs()
        }

        bcfs.map {
            it.benchClass()
        }.flatten().forEach {
            if (it.second.benchs.isNotEmpty()) {
                saveExecInfos(it.first, it.second)
            }
        }
    }

    private fun searchStateObjects() {
        som = StateObjectManager()

        val filePaths = sourceDirectory.walkTopDown().filter { f ->
            f.isFile && f.extension == "java"
        }.map { it.absolutePath }.toList().toTypedArray()

        val bcfs = mutableListOf<JdtBenchClassFinder>()
        parse(filePaths, bcfs) { javaUnit, _ ->
            val bcf = JdtBenchClassFinder { node, _ ->
                val cv = JdtBenchStateObjectVisitor(som)
                node.accept(cv)
            }
            javaUnit.accept(bcf)
        }
    }

    private fun parse(filePaths: Array<String>, bcfs: MutableList<JdtBenchClassFinder>, action: (javaUnit: CompilationUnit, bcfs: MutableList<JdtBenchClassFinder>) -> Unit) {
        val parser = ASTParser.newParser(AST.getJLSLatest())
        parser.setKind(ASTParser.K_COMPILATION_UNIT)
        parser.setEnvironment(arrayOf<String>(), arrayOf(sourceDirectory.absolutePath), arrayOf("UTF-8"), true)
        parser.setResolveBindings(true)
        parser.setBindingsRecovery(true)
        parser.createASTs(filePaths, null, arrayOf(), object : FileASTRequestor() {
            override fun acceptAST(sourceFilePath: String, javaUnit: CompilationUnit) {
                action(javaUnit, bcfs)
            }
        }, null)
    }

    override fun methodHashes(): Either<String, Map<Method, ByteArray>> {
        if (!parsed) {
            val a = all()
            a.getOrElse {
                return Either.Left(it)
            }.right()
        }

        val ret = mutableMapOf<Method, ByteArray>()
        bcfs.map {
            it.benchClass()
        }.flatten().map { it.second }.map { it.methodHashes }.forEach {
            ret.putAll(it)
        }

        return Either.Right(ret)
    }

    override fun methodNumberOfLines(): Either<String, Map<Method, Int>> {
        if (!parsed) {
            val a = all()
            a.getOrElse {
                return Either.Left(it)
            }.right()
        }

        val ret = mutableMapOf<Method, Int>()
        bcfs.map {
            it.benchClass()
        }.flatten().map { it.second }.map { it.numberOfLines }.forEach {
            ret.putAll(it)
        }

        return Either.Right(ret)
    }
}