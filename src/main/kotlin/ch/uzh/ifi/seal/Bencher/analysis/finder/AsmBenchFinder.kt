package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import org.funktionale.either.Either
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths

class AsmBenchFinder(private val jar: File, pkgPrefix: String = "") : BenchmarkFinder {
    private val pathPrefix: String = pkgPrefix.replaceDotsWithSlashes

    private var parsed: Boolean = false
    private lateinit var benchs: List<Benchmark>
    private val setups: MutableMap<Benchmark, Set<SetupMethod>> = mutableMapOf()
    private val tearDowns: MutableMap<Benchmark, Set<TearDownMethod>> = mutableMapOf()

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

    override fun setups(b: Benchmark): Collection<SetupMethod> = setups[b] ?: setOf()

    override fun tearDowns(b: Benchmark): Collection<TearDownMethod> = tearDowns[b] ?: setOf()

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
                val benchs = cv.benchs()
                benchs.forEach { b ->
                    setups[b] = cv.setups()
                    tearDowns[b] = cv.tearDowns()
                }
                benchs
            }.flatten().toList()

    companion object {
        private val tmpDirPrefix = "bencher-AsmBenchFinder-"
        private val jarExtractionDir = "jar"
    }
}
