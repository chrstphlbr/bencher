package ch.uzh.ifi.seal.bencher.analysis.coverage.dyn.javacallgraph

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoverageExecutor
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoverageInclusions
import ch.uzh.ifi.seal.bencher.analysis.coverage.IncludeAll
import ch.uzh.ifi.seal.bencher.analysis.coverage.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CUF
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitMethod
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnitResult
import ch.uzh.ifi.seal.bencher.analysis.coverage.dyn.AbstractDynamicCoverage
import ch.uzh.ifi.seal.bencher.analysis.finder.MethodFinder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.BufferedReader
import java.io.File
import java.io.Reader
import java.nio.file.Path
import java.time.Duration
import kotlin.streams.asSequence

class JavaCallgraphDC(
    benchmarkFinder: MethodFinder<Benchmark>,
    javaSettings: JavaSettings,
    oneCovForParameterizedBenchmarks: Boolean = true,
    inclusion: CoverageInclusions = IncludeAll,
    timeOut: Duration = Duration.ofMinutes(10),
) : AbstractDynamicCoverage(
    benchmarkFinder = benchmarkFinder,
    javaSettings = javaSettings,
    oneCoverageForParameterizedBenchmarks = oneCovForParameterizedBenchmarks,
    timeOut = timeOut,
), CoverageExecutor {

    private val inclusionsString: String = inclusions(inclusion)

    override fun parseCoverageUnitResults(r: Reader, b: Benchmark): Either<String, Set<CoverageUnitResult>> {
        val br = BufferedReader(r)
        val bpm = b.toPlainMethod()
        var benchLevel = 0
        val rs: Set<CoverageUnitResult> = br.lines().asSequence()
            .filter {
                if (it.startsWith("START")) {
                    benchLevel = it.substringAfter("_").toInt()
                    false
                } else {
                    true
                }
            }
            .map {
                parseCoverageUnitResult(bpm, benchLevel, it).getOrElse { err ->
                    log.error("Could not parse CoverageUnitResult: $err")
                    null
                }
            }
            .filter { it != null }
            .map { it as CoverageUnitResult }
            .toSet()

        return Either.Right(rs)
    }

    private fun parseCoverageUnitsCalltrace(r: BufferedReader, b: Benchmark): Either<String, List<CoverageUnitResult>> {
        val benchLine = calltraceBench(b)
        val bpm = b.toPlainMethod()
        var inBenchCG = false
        var benchLevel = 0
        val rs: List<CoverageUnitResult> = r.lines().asSequence()
            .filter {
                if (it.contains(benchLine)) {
                    inBenchCG = !inBenchCG
                    benchLevel = parseStackDepth(it)
                    false
                } else {
                    inBenchCG
                }
            }
            .filter { it.startsWith(">") }
            .map { parseCoverageUnitResult(bpm, benchLevel, it).getOrNull() }
            .filter { it != null }
            .map { it as CoverageUnitResult }
            .toList()

        return Either.Right(rs)
    }

    private fun parseStackDepth(l: String): Int = l.substringAfter("[").substringBefore("]").toInt()

    private val charSet = setOf('[', ']', ':', '(', ')')

    private fun parseCoverageUnitResult(from: Method, benchLevel: Int, l: String): Either<String, CoverageUnitResult> {
        var stackDepth = 0
        val benchClass = StringBuilder()
        val benchMethod = StringBuilder()
        val benchParams = StringBuilder()
        var state = 0

        loop@ for (c in l) {
            when (c) {
                '[' -> state++
                ']' -> state++
                ':' -> state++
                '(' -> state++
                ')' -> state++
            }

            if (charSet.contains(c)) {
                continue
            }

            when (state) {
                1 -> stackDepth = c.toString().toInt()
                4 -> benchClass.append(c)
                5 -> benchMethod.append(c)
                6 -> benchParams.append(c)
                7 -> break@loop
            }
        }

        val bps = benchParams.toString()
        val ps = if (bps.isBlank()) {
            listOf()
        } else {
            bps.splitToSequence(",").toList()
        }

        val r = CUF.covered(
            of = from,
            unit = CoverageUnitMethod(
                MF.plainMethod(
                    clazz = benchClass.toString(),
                    name = benchMethod.toString(),
                    params = ps,
                )
            ),
            level = stackDepth - benchLevel,
        )

        return Either.Right(r)
    }

    override fun jvmArgs(b: Benchmark): String = String.format(JVM_ARGS, jcgAgentJar, calltraceBench(b), inclusionsString)

    override fun resultFileName(b: Benchmark): String = CALLTRACE_FILE_NAME

    override fun transformResultFile(jar: Path, dir: File, b: Benchmark, resultFile: File): Either<String, File> =
        Either.Right(resultFile)

    private fun calltraceBench(b: Benchmark, escapeDollar: Boolean = false): String =
        "${b.clazz}:${b.name}".let {
            if (escapeDollar) {
                it.replace("$", "\\$")
            } else {
                it
            }
        }

    private fun inclusions(i: CoverageInclusions): String =
        when (i) {
            is IncludeAll -> ".*"
            is IncludeOnly -> i.includes.joinToString(separator = ",") { "$it.*" }
        }

    companion object {
        private val log: Logger = LogManager.getLogger(JavaCallgraphDC::class.java.canonicalName)

        //  JVM arguments
        //   1. java-callgraph agent jar path (e.g., jcgAgentJar)
        //   2. benchmark of format: clazz:method(param1,param2) (e.g., calltraceBench)
        //   3. call graph inclusions (e.g., inclusionsString)
        private const val JVM_ARGS = "-javaagent:%s=bench=%s;incl=%s"
        private val jcgAgentJar = "jcg_agent.jar.zip".fileResource().absolutePath

        private const val CALLTRACE_FILE_NAME = "calltrace.txt"
    }
}
