package ch.uzh.ifi.seal.bencher.analysis.finder

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.WalaProperties
import ch.uzh.ifi.seal.bencher.analysis.coverage.sta.bencherMethod
import ch.uzh.ifi.seal.bencher.analysis.sourceCode
import com.ibm.wala.core.util.config.AnalysisScopeReader
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.nio.file.Path
import java.time.Duration

class JarBenchFinder(
    val jar: Path,
    val javaSettings: JavaSettings,
    val removeDuplicates: Boolean = true,
) : MethodFinder<Benchmark> {

    private val defaultTimeout = Duration.ofMinutes(1)

    private var parsed = false
    private lateinit var benchmarks: List<Benchmark>
    private lateinit var ch: ClassHierarchy

    private val env: Map<String, String> = mapOfNotNull(javaSettings.homePair())

    override fun all(): Either<String, List<Benchmark>> {
        if (!parsed) {
            benchmarks = generateBenchs().getOrElse {
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

        val scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(jar.toAbsolutePath().toString(), ef)
        ch = ClassHierarchyFactory.make(scope)

        return benchs(jar.toAbsolutePath())
    }

    private fun benchs(jarPath: Path): Either<String, List<Benchmark>> {
        val benchsParams = executeBenchCmd(jarCmd(jarPath, true, javaSettings), env)
        if (benchsParams.isRight()) {
            // got benchmarks including parameters
            return benchsParams
        }
        // bench not parsable including params
        return executeBenchCmd(jarCmd(jarPath, false, javaSettings), env)
    }

    private fun executeBenchCmd(cmd: String, env: Map<String, String>): Either<String, List<Benchmark>> {
        val (success, out, err) = cmd.runCommand(File(Constants.homeDir), defaultTimeout, env)
        if (!success) {
            // execution timed out
            return Either.Left("Execution '${cmd}' timed out ")
        }

        if (!err.isNullOrBlank()) {
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

        if (!lines[0].startsWith(JAR_CMD_FIRST_LINE)) {
            return Either.Left("No benchmark out:\n${out}")
        }

        var currentBench: Benchmark? = null
        val benchs = mutableListOf<Benchmark>()
        val seen = mutableSetOf<Benchmark>()
        for (i in 1 until lines.size) {
            val currentLine = lines[i]

            if (currentLine.startsWith(JAR_CMD_PARAM_LINE)) {
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
                jmhParams = listOf(),
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
        private const val ADD_OPENS_JAVA_BASE_JAVA_IO = "--add-opens=java.base/java.io=ALL-UNNAMED"
        private const val JAR_CMD_BENCHMARKS_WITH_PARAMS = "java %s -jar %s -lp"
        private const val JAR_CMD_BENCHMARKS = "java %s -jar %s -l"
        private const val JAR_CMD_FIRST_LINE = "Benchmarks:"
        private const val JAR_CMD_PARAM_LINE = "  param"

        private val log: Logger = LogManager.getLogger(JarBenchFinder::class.java.canonicalName)

        private fun jarCmd(jarPath: Path, params: Boolean, javaSettings: JavaSettings): String {
            val jvmArgs = StringBuilder()
                .append(ADD_OPENS_JAVA_BASE_JAVA_IO) // needed for some JMH versions when they are executed with newer Java versions
            if (javaSettings.jvmArgs != null) {
                jvmArgs
                    .append(" ")
                    .append(javaSettings.jvmArgs)
            }

            val cmd = if (params) {
                JAR_CMD_BENCHMARKS_WITH_PARAMS
            } else {
                JAR_CMD_BENCHMARKS
            }

            return String.format(cmd, jvmArgs, jarPath)
        }
    }
}
