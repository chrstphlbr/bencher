package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.fileResource
import ch.uzh.ifi.seal.bencher.replaceSlashesWithDots
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

class AsmBenchVisitorTest : AbstractAsmBenchFinderTest() {
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

    fun benchmarks(jarDir: File): Set<Benchmark> =
            jarDir.walkTopDown().filter { f ->
                f.isFile && f.extension == "class" && f.absolutePath.startsWith(Paths.get(jarDir.absolutePath, pathPrefix).toString())
            }.map { f ->
                val cr = ClassReader(FileInputStream(f))
                val opcode = Opcodes.ASM6
                val cv = AsmBenchClassVisitor(
                        api = opcode,
                        cv = null,
                        className = f.absolutePath.replace(".class", "").substring(f.absolutePath.indexOf(pathPrefix)).replaceSlashesWithDots
                )
                cr.accept(cv, opcode)
                cv.benchs()
            }.flatten().toSet()

    @Test
    fun twoBenchs121() {
        val url = JarTestHelper.jar2BenchsJmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")


        val eJarFolder = JarHelper.extractJar(tmpDir, url.absolutePath, "jar")
        if (eJarFolder.isLeft()) {
            Assertions.fail<String>("Could not extract jar file: ${eJarFolder.left().get()}")
        }

        val jarDir = eJarFolder.right().get()
        val bs = benchmarks(jarDir)
        assertTwoBenchs(bs)
    }

    @Test
    fun fourBenchs121() {
        val url = JarTestHelper.jar4BenchsJmh121.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")

        val eJarFolder = JarHelper.extractJar(tmpDir, url.absolutePath, "jar")
        if (eJarFolder.isLeft()) {
            Assertions.fail<String>("Could not extract jar file: ${eJarFolder.left().get()}")
        }

        val jarDir = eJarFolder.right().get()
        val bs = benchmarks(jarDir)
        assertFourBenchs(bs)
    }

    @Test
    fun twoBenchs110() {
        val url = JarTestHelper.jar2BenchsJmh110.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")

        val eJarFolder = JarHelper.extractJar(tmpDir, url.absolutePath, "jar")
        if (eJarFolder.isLeft()) {
            Assertions.fail<String>("Could not extract jar file: ${eJarFolder.left().get()}")
        }

        val jarDir = eJarFolder.right().get()
        val bs = benchmarks(jarDir)

        assertTwoBenchs(bs)
    }

    @Test
    fun fourBenchs110() {
        val url = JarTestHelper.jar4BenchsJmh110.fileResource()
        Assertions.assertNotNull(url, "Could not get resource")

        val eJarFolder = JarHelper.extractJar(tmpDir, url.absolutePath, "jar")
        if (eJarFolder.isLeft()) {
            Assertions.fail<String>("Could not extract jar file: ${eJarFolder.left().get()}")
        }

        val jarDir = eJarFolder.right().get()
        val bs = benchmarks(jarDir)

        assertFourBenchs(bs)
    }

    companion object {
        val asmBencherTmpDir = "bencher-test"
        val pathPrefix = "org/sample"
    }
}
