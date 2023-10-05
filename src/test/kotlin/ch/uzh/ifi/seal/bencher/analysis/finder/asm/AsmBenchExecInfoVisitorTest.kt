package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration
import ch.uzh.ifi.seal.bencher.fileResource
import ch.uzh.ifi.seal.bencher.replaceFileSeparatorWithDots
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths

typealias jmhConfigs = Pair<Map<Class, ExecutionConfiguration>, Map<Benchmark, ExecutionConfiguration>>

class AsmBenchExecInfoVisitorTest : AbstractAsmBenchExecInfoTest() {

    private lateinit var tmpDir: File

    @BeforeEach
    fun setup() {
        val p = Files.createTempDirectory(asmBencherTmpDir)
        tmpDir = File(p.toUri())
    }

    @AfterEach
    fun tearDown() {
        val del = tmpDir.deleteRecursively()
        if (!del) {
            println("Could not delete tmp dir ${tmpDir.absolutePath}")
        }
    }

    private fun execConfigs(jarDir: File): jmhConfigs =
            jarDir.walkTopDown().filter { f ->
                f.isFile && f.extension == "class" && f.absolutePath.startsWith(Paths.get(jarDir.absolutePath, AsmBenchVisitorTest.pathPrefix).toString())
            }.map { f ->
                val cr = ClassReader(FileInputStream(f))
                val opcode = Opcodes.ASM7
                val className = f.absolutePath.replace(".class", "")
                    .substring(f.absolutePath.indexOf(AsmBenchVisitorTest.pathPrefix)).replaceFileSeparatorWithDots
                val cv = AsmBenchClassVisitor(
                    api = opcode,
                    cv = null,
                    className = className
                )
                cr.accept(cv, opcode)

                val cei = cv.classExecInfo()
                    .map {
                        val c = Class(className)
                        mapOf(Pair(c, it))
                    }
                    .getOrNull() ?: mapOf()

                Pair(cei, cv.benchExecInfos())
            }.fold(Pair(mapOf(), mapOf())) { acc, (c, b) ->
                Pair(acc.first + c, acc.second + b)
            }

    @Test
    fun test() {
        val url = JarTestHelper.jar4BenchsJmh121v2.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")

        val jarDir = JarHelper.extractJar(tmpDir, url.absoluteFile, "jar").getOrElse {
            Assertions.fail<String>("Could not extract jar file: $it")
            return
        }
        val (ccs, bcs) = execConfigs(jarDir)

        assertClassConfigs(ccs)
        assertBenchConfigs(bcs)
    }

    companion object {
        private const val asmBencherTmpDir = "bencher-test"
    }
}