package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn.javacallgraph

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.MF
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGInclusions
import ch.uzh.ifi.seal.bencher.analysis.callgraph.IncludeAll
import ch.uzh.ifi.seal.bencher.analysis.callgraph.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn.AbstractDynamicCoverage
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.RF
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.ReachabilityResult
import ch.uzh.ifi.seal.bencher.analysis.finder.MethodFinder
import ch.uzh.ifi.seal.bencher.fileResource
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.funktionale.either.Either
import java.io.BufferedReader
import java.io.File
import java.io.Reader
import java.nio.file.Path
import java.time.Duration
import kotlin.streams.asSequence

class JavaCallgraphDCG(
        benchmarkFinder: MethodFinder<Benchmark>,
        oneCGForParameterizedBenchmarks: Boolean = true,
        inclusion: CGInclusions = IncludeAll,
        timeOut: Duration = Duration.ofMinutes(10)
) : AbstractDynamicCoverage(
        benchmarkFinder = benchmarkFinder,
        oneCoverageForParameterizedBenchmarks = oneCGForParameterizedBenchmarks,
        timeOut = timeOut
), CGExecutor {

    private val inclusionsString: String = inclusions(inclusion)

    override fun parseReachabilityResults(r: Reader, b: Benchmark): Either<String, Set<ReachabilityResult>> {
        val br = BufferedReader(r)
        val bpm = b.toPlainMethod()
        var benchLevel = 0
        val rs: Set<ReachabilityResult> = br.lines().asSequence()
                .filter {
                    if (it.startsWith("START")) {
                        benchLevel = it.substringAfter("_").toInt()
                        false
                    } else {
                        true
                    }
                }
                .map {
                    val erb = parseReachabilityResult(bpm, benchLevel, it)
                    if (erb.isLeft()) {
                        log.error("Could not parse ReachabilityResult: ${erb.left().get()}")
                        null
                    } else {
                        erb.right().get()
                    }
                }
                .filter { it != null }
                .map { it as ReachabilityResult }
                .toSet()

        return Either.right(rs)
    }

    private fun parseReachabilityResultsCalltrace(r: BufferedReader, b: Benchmark): Either<String, List<ReachabilityResult>> {
        val benchLine = calltraceBench(b)
        val bpm = b.toPlainMethod()
        var inBenchCG = false
        var benchLevel = 0
        val rs: List<ReachabilityResult> = r.lines().asSequence()
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
                .map {
                    val erb = parseReachabilityResult(bpm, benchLevel, it)
                    if (erb.isLeft()) {
                        null
                    } else {
                        erb.right().get()
                    }
                }
                .filter { it != null }
                .map { it as ReachabilityResult }
                .toList()

        return Either.right(rs)
    }

    private fun parseStackDepth(l: String): Int = l.substringAfter("[").substringBefore("]").toInt()

    private val charSet = setOf('[', ']', ':', '(', ')')

    private fun parseReachabilityResult(from: Method, benchLevel: Int, l: String): Either<String, ReachabilityResult> {
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

        val r = RF.reachable(
                from = from,
                to = MF.plainMethod(
                        clazz = benchClass.toString(),
                        name = benchMethod.toString(),
                        params = ps
                ),
                level = stackDepth - benchLevel
        )

        return Either.right(r)
    }

    override fun jvmArgs(b: Benchmark): String = String.format(jvmArgs, jcgAgentJar, calltraceBench(b), inclusionsString)

    override fun resultFileName(b: Benchmark): String = calltraceFileName

    override fun transformResultFile(jar: Path, dir: File, b: Benchmark, resultFile: File): Either<String, File> =
            Either.right(resultFile)

    private fun calltraceBench(b: Benchmark, escapeDollar: Boolean = false): String =
            "${b.clazz}:${b.name}".let {
                if (escapeDollar) {
                    it.replace("$", "\\$")
                } else {
                    it
                }
            }

    private fun inclusions(i: CGInclusions): String =
            when (i) {
                is IncludeAll -> ".*"
                is IncludeOnly -> i.includes.joinToString(separator = ",") { "$it.*" }
            }

    companion object {
        val log: Logger = LogManager.getLogger(JavaCallgraphDCG::class.java.canonicalName)

        //  JVM arguments
        //   1. java-callgraph agent jar path (e.g., jcgAgentJar)
        //   2. benchmark of format: clazz:method(param1,param2) (e.g., calltraceBench)
        //   3. call graph inclusions (e.g., inclusionsString)
        private val jvmArgs = "-javaagent:%s=bench=%s;incl=%s"
        private val jcgAgentJar = "jcg_agent.jar.zip".fileResource().absolutePath

        private const val calltraceFileName = "calltrace.txt"
    }
}
