package ch.uzh.ifi.seal.bencher.analysis.finder

import arrow.core.Either
import arrow.core.getOrHandle
import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.WalaProperties
import ch.uzh.ifi.seal.bencher.analysis.coverage.sta.bencherMethod
import ch.uzh.ifi.seal.bencher.analysis.sourceCode
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.util.config.AnalysisScopeReader
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Path
import java.time.Duration

class JarBenchFinder(val jar: Path, val removeDuplicates: Boolean = true) : MethodFinder<Benchmark> {

    private val defaultTimeout = Duration.ofMinutes(1)

    private var parsed = false
    private lateinit var benchmarks: List<Benchmark>
    private lateinit var ch: ClassHierarchy

    override fun all(): Either<String, List<Benchmark>> {
        if (!parsed) {
            benchmarks = generateBenchs().getOrHandle {
                return Either.Left("Could not generate benchmarks: $it")
            }
            parsed = true
        }
        return Either.Right(benchmarks)
    }

    private fun generateBenchs(): Either<String, List<Benchmark>> {
        val ef = WalaProperties.exclFile.fileResource()
        if (!ef.exists()) {
            return Either.Left("Exclusions file '${WalaProperties.exclFile}' does not exist")
        }

        val scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jar.toAbsolutePath().toString(), ef)
        ch = ClassHierarchyFactory.make(scope)

        return benchs(jar.toAbsolutePath())
    }

    private fun benchs(jarPath: Path): Either<String, List<Benchmark>> {
        val cmd = String.format(jarCmdBenchmarksWithParams, jarPath)
        val benchsParams = executeBenchCmd(cmd)
        if (benchsParams.isRight()) {
            // got benchmarks including parameters
            return benchsParams
        }
        // bench not parsable including params
        return executeBenchCmd(String.format(jarCmdBenchmarks, jarPath))
    }

    private fun executeBenchCmd(cmd: String): Either<String, List<Benchmark>> {
        val (success, out, err) = cmd.runCommand(File(Constants.homeDir), defaultTimeout)
        if (!success) {
            // execution timed out
            return Either.Left("Execution '${cmd}' timed out ")
        }

        if (err != null && err.isNotBlank()) {
//            return Either.left(err)
            // log the error from stderr instead of fail the program
            log.warn(err)
        }

        if (out == null) {
            return Either.Left("No output from '${cmd}' (and no error)")
        }

        return parseBenchs(out)
    }

    private fun parseBenchs(out: String, cmd: String = ""): Either<String, List<Benchmark>> {
        val lines = out.split("\n")

        if (lines.isEmpty()) {
            return Either.Left("No output from '${cmd}' (and no error)")
        }

        if (!lines[0].startsWith(jarCmdFirstLine)) {
            return Either.Left("No benchmark out:\n${out}")
        }

        var currentBench: Benchmark? = null
        val benchs = mutableListOf<Benchmark>()
        val seen = mutableSetOf<Benchmark>()
        for (i in 1 until lines.size) {
            val currentLine = lines[i]

            if (currentLine.startsWith(jarCmdParamLine)) {
                // param line
                currentBench = currentBench!!.copy(jmhParams = currentBench.jmhParams + parseJmhParams(currentLine))
            } else {
                // benchmark line

                // add last bench
                if (currentBench != null) {
                    if (!removeDuplicates || !seen.contains(currentBench)) {
                        benchs.add(currentBench)
                        seen.add(currentBench)
                    }
                }

                if (currentLine.isBlank()) {
                    currentBench = null
                    continue
                }

                currentBench = parseBench(currentLine)
            }
        }

        if (currentBench != null && (!removeDuplicates || !seen.contains(currentBench))) {
            benchs.add(currentBench)
        }

        return Either.Right(benchs.toList())
    }

    private fun parseBench(bench: String): Benchmark {
        val clazz = addDollarsForNestedClasses(bench.substringBeforeLast("."))
        val method = bench.substringAfterLast(".")
        return MF.benchmark(
                clazz = clazz,
                name = method,
                params = getParams(clazz, method),
                jmhParams = listOf()
        )
    }

    private fun addDollarsForNestedClasses(c: String): String {
        var change = false
        var afterDot = true

        val ca = CharArray(c.length)
        c.forEachIndexed { i, char ->
            ca[i] = if (afterDot) {
                if (!change && char.isUpperCase()) {
                    change = true
                }
                afterDot = false
                char
            } else if (char == '.') {
                afterDot = true
                if (change) {
                    '$'
                } else {
                    char
                }
            } else {
                char
            }
        }

        return String(ca)
    }

    private fun getParams(clazz: String, method: String): List<String> {
        val c = ch.find { it.name.toUnicodeString().sourceCode == clazz } ?: return listOf()
        val m = c.declaredMethods.find { it.name.toUnicodeString() == method } ?: return listOf()
        return m.bencherMethod().params
    }

    private fun parseJmhParams(jmhParam: String): List<Pair<String, String>> {
        if (jmhParam.isBlank()) {
            return listOf()
        }

        val paramName = jmhParam.substringAfter("\"").substringBefore("\"")
        val paramVals = jmhParam.substringAfter("{").substringBefore("}").split(", ")
        return paramVals.map { Pair(paramName, it) }
    }

    companion object {
        val jarCmdBenchmarksWithParams = "java -jar %s -lp"
        val jarCmdBenchmarks = "java -jar %s -l"
        val jarCmdFirstLine = "Benchmarks:"
        val jarCmdParamLine = "  param"

        val log = LogManager.getLogger(JarBenchFinder::class.java.canonicalName)
    }
}
