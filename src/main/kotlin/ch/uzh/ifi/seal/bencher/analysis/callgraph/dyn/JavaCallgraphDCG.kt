package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn

import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.*
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.*
import ch.uzh.ifi.seal.bencher.analysis.finder.MethodFinder
import org.apache.logging.log4j.LogManager
import org.funktionale.either.Either
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime
import kotlin.streams.toList

class JavaCallgraphDCG(
        private val benchmarkFinder: MethodFinder<Benchmark>,
        private val oneCGForParameterizedBenchmarks: Boolean = true,
        private val inclusion: CGInclusions = IncludeAll,
        private val timeOut: Duration = Duration.ofMinutes(1)
) : CGExecutor {

    private val inclusionsString = inclusions()

    override fun get(jar: Path): Either<String, CGResult> {
        val ebs = benchmarkFinder.all()
        if (ebs.isLeft()) {
            return Either.left(ebs.left().get())
        }
        val bs: List<Benchmark> = ebs.right().get()

        val total = bs.size
        log.info("start generating CGs")
        val startCGS = LocalDateTime.now()

        val cgs: Map<Method, Reachabilities> = bs.mapIndexed { i, b ->
            val l = logTimes(b, i, total, "CG for")
            val ecg = callgraphs(jar, b)
            l()
            if (ecg.isLeft()) {
                return Either.left(ecg.left().get())
            }
            ecg.right().get()
        }.flatten().toMap()

        val endCGS = LocalDateTime.now()
        log.info("finished generating CGs in ${Duration.between(startCGS, endCGS)}")

        return Either.right(CGResult(cgs))
    }

    private fun callgraphs(jar: Path, b: Benchmark): Either<String, List<Pair<Benchmark, Reachabilities>>> {
        val bs = b.parameterizedBenchmarks()

        val p = Files.createTempDirectory(tmpDirPrefix)
        val tmpDir = File(p.toUri())

        val total = bs.size
        if (total < 1) {
            return Either.left("Expected at least 1 benchmark but was $total")
        }

        try {
            val ret: List<Pair<Benchmark, Reachabilities>> = if (oneCGForParameterizedBenchmarks) {
                val benchPair = callgraphsBench(jar, 0, total, tmpDir, bs[0])
                if (benchPair == null) {
                    listOf()
                } else {
                    listOf(Pair(b, replaceFrom(b, benchPair.second)))
                }
            } else {
                callgraphsParam(jar, total, tmpDir, bs)
            }
            return Either.right(ret)
        } finally {
            JarHelper.deleteTmpDir(tmpDir)
        }
    }

    private fun replaceFrom(b: Benchmark, rs: Reachabilities): Reachabilities =
            b.toPlainMethod().let { pb ->
                Reachabilities(
                        start = b,
                        reachabilities = rs.reachabilities().mapNotNull {
                            when (it) {
                                is NotReachable -> null
                                is PossiblyReachable -> RF.possiblyReachable(
                                        from = pb,
                                        to = it.to,
                                        level = it.level,
                                        probability = it.probability
                                )
                                is Reachable -> RF.reachable(
                                        from = pb,
                                        to = it.to,
                                        level = it.level
                                )
                            }
                        }.toSet()
                )
            }

    private fun callgraphsBench(jar: Path, i: Int, total: Int, tmpDir: File, b: Benchmark): Pair<Benchmark, Reachabilities>? {
        val cs = cmdStr(jar, b)

        log.debug("Param bench $b: ${i+1}/$total; '$cs'")

        val l = logTimesParam(b, i, total, "CG for parameterized benchmark")
        val ers = exec(cs, tmpDir, b)
        return try {
            if (ers.isLeft()) {
                log.error("Could not retrieve DCG for $b with '$cs': ${ers.left().get()}")
                null
            } else {
                val rs = ers.right().get()
                Pair(b, rs)
            }
        } finally {
            l()
        }
    }

    private fun callgraphsParam(jar: Path, total: Int, tmpDir: File, bs: List<Benchmark>): List<Pair<Benchmark, Reachabilities>> =
            bs.mapIndexedNotNull { i, pb -> callgraphsBench(jar, i, total, tmpDir, pb) }

    private fun logTimesParam(b: Benchmark, i: Int, total: Int, text: String): () -> Unit =
            if (total <= 1) {
                {}
            } else {
                logTimes(b, i, total, text)
            }

    private fun logTimes(b: Benchmark, i: Int, total: Int, text: String): () -> Unit {
        log.info("start $text $b (${i+1}/$total)")
        val start = LocalDateTime.now()
        return {
            val end = LocalDateTime.now()
            log.info("finished $text $b (${i+1}/$total) in ${Duration.between(start, end)}")
        }
    }

    private fun exec(cmd: String, dir: File, b: Benchmark): Either<String, Reachabilities> {
        val (ok, out, err) = cmd.runCommand(dir, timeOut)
        if (!ok) {
            return Either.left("Execution of '$cmd' did not finish within $timeOut")
        }

        if (out != null && out.isNotBlank()) {
            log.debug("Process out: $out")
        }

        if (err != null && err.isNotBlank()) {
            log.debug("Process err: $err")
        }

        return parseReachabilities(dir, b)
    }

    private fun parseReachabilities(dir: File, b: Benchmark): Either<String, Reachabilities> {
        val fn = "$dir${File.separator}$calltraceFileName"
        val f = File(fn)

        if (!f.isFile) {
            return Either.left("Not a file: $fn")
        }

        if (!f.exists()) {
            return Either.left("File does not exist: $fn")
        }

        val fr = BufferedReader(FileReader(f))
        try {
            val errs = parseReachabilityResultsSimple(fr, b)
            if (errs.isLeft()) {
                return Either.left(errs.left().get())
            }

            val rrss = mutableSetOf<Method>()

            val rrs = errs.right().get()
            val srrs = rrs.toSortedSet(ReachabilityResultComparator)
                    .filter {
                        val m = it.to
                        if (rrss.contains(m)) {
                            false
                        } else {
                            rrss.add(m)
                            true
                        }
                    }.toSet()

            log.info("CG for $b has ${srrs.size} reachable nodes (from ${rrs.size} traces)")

            val rs = Reachabilities(
                    start = b,
                    reachabilities = srrs
            )

            return Either.right(rs)
        } finally {
            try {
                fr.close()
            } catch (e: IOException) {
                log.warn("Could not close file output stream of '$fn'")
            }
        }
    }

    private fun parseStackDepth(l: String): Int = l.substringAfter("[").substringBefore("]").toInt()

    private fun parseReachabilityResultsSimple(r: BufferedReader, b: Benchmark): Either<String, List<ReachabilityResult>> {
        val bpm = b.toPlainMethod()
        var benchLevel = 0
        val rs: List<ReachabilityResult> = r.lines()
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
                .toList()

        return Either.right(rs)
    }

    private fun parseReachabilityResultsCalltrace(r: BufferedReader, b: Benchmark): Either<String, List<ReachabilityResult>> {
        val benchLine = calltraceBench(b)
        val bpm = b.toPlainMethod()
        var inBenchCG = false
        var benchLevel = 0
        val rs: List<ReachabilityResult> = r.lines()
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

    private val charSet = setOf('[', ']', ':', '(', ')')

    private fun parseReachabilityResult(from: Method, benchLevel: Int, l: String): Either<String, ReachabilityResult> {
        var stackDepth = 0
        val benchClass = StringBuilder()
        val benchMethod = StringBuilder()
        val benchParams = StringBuilder()
        var state = 0

        loop@for (c in l) {
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

    private fun calltraceBench(b: Benchmark, escapeDollar: Boolean = false): String =
            "${b.clazz}:${b.name}".let {
                if (escapeDollar) {
                    it.replace("$", "\\$")
                } else {
                    it
                }
            }

    private fun cmdStr(jar: Path, b: Benchmark): String =
            if (b.jmhParams.isEmpty()) {
                String.format(cmd, jar, jcgAgentJar, calltraceBench(b, false), inclusionsString, benchName(b))
            } else {
                String.format(cmdParam, jar, jcgAgentJar, calltraceBench(b, false), inclusionsString, jmhParams(b), benchName(b))
            }

    private fun inclusions(): String =
            when (inclusion) {
                is IncludeAll -> ".*"
                is IncludeOnly -> inclusion.includes.joinToString(separator = ",") { "$it.*" }
            }

    private fun benchName(b: Benchmark): String = "${b.clazz.replace("$", ".")}.${b.name}"

    private fun jmhParams(b: Benchmark): String =
            b.jmhParams.joinToString(separator = " ") { "-p ${it.first}=${it.second}" }

    companion object {
        val log = LogManager.getLogger(JavaCallgraphDCG::class.java.canonicalName)

        // arguments:
        // For use in shell (e.g., bash) the constructed command string needs escaping of
        // (1) dollar-signs ($) and
        // (2) double-quotes (")
        // around the -jvmArgs argument and the JMH benchmark regex
        //   1. benchmark jar path
        //   2. java-callgraph agent jar path
        //   3. benchmark of format: clazz:method(param1,param2)
        //   3. call graph inclusions
        //   4. JMH parameter string (required for cmdParam and optional for cmd)
        //   5. JMH benchmark
        private const val baseCmd = "java -jar %s -jvmArgs=-javaagent:%s=bench=%s;incl=%s -wi 0 -i 1 -f 1 -r 1 -w 1 -bm ss"
        private const val cmd = "$baseCmd ^%s\$"
        private const val cmdParam = "$baseCmd %s ^%s\$"

        private val jcgAgentJar = "jcg_agent.jar".fileResource().absolutePath
        private const val calltraceFileName = "calltrace.txt"

        private const val tmpDirPrefix = "bencher-java-callgraph-"
    }
}
