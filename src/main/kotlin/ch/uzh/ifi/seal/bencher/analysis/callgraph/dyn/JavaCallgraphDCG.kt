package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn

import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.*
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
import kotlin.streams.toList

class JavaCallgraphDCG(
        private val benchmarkFinder: MethodFinder<Benchmark>,
        private val inclusion: CGInclusions = IncludeAll,
        private val timeOut: Duration = Duration.ofMinutes(1)
) : CGExecutor {

    private val inclusionsString = inclusions()

    override fun get(jar: Path): Either<String, CGResult> {
        val ebs = benchmarkFinder.all()
        if (ebs.isLeft()) {
            return Either.left(ebs.left().get())
        }
        val bs: Iterable<Benchmark> = ebs.right().get()

        val cgs: Map<Method, Reachabilities> = bs.flatMap {
            val ecg = callgraphs(jar, it)
            if (ecg.isLeft()) {
                return Either.left(ecg.left().get())
            }
            ecg.right().get()
        }.toMap()

        return Either.right(CGResult(cgs))
    }

    private fun callgraphs(jar: Path, b: Benchmark): Either<String, List<Pair<Benchmark, Reachabilities>>> {
        val bs = b.parameterizedBenchmarks()

        val p = Files.createTempDirectory(tmpDirPrefix)
        val tmpDir = File(p.toUri())

        try {
            val ret: List<Pair<Benchmark, Reachabilities>> = bs.mapNotNull { pb ->
                val cs = cmdStr(jar, pb)
                val ers = exec(cs, tmpDir, pb)
                if (ers.isLeft()) {
                    log.error("Could not retrieve DCG for $pb with '$cs': ${ers.left().get()}")
                    return@mapNotNull null
                }
                val rs = ers.right().get()
                Pair(pb, rs)
            }
            return Either.right(ret)
        } finally {
            JarHelper.deleteTmpDir(tmpDir)
        }
    }

    private fun exec(cmd: String, dir: File, b: Benchmark): Either<String, Reachabilities> {
        val (ok, _, err) = cmd.runCommand(dir, timeOut)
        if (!ok) {
            return Either.left("Execution of '$cmd' did not finish within $timeOut")
        }

        if (err != null && err.isNotBlank()) {
            return Either.left(err)
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
            val errs = parseReachabilityResults(fr, b)
            if (errs.isLeft()) {
                return Either.left(errs.left().get())
            }

            val rrss = mutableSetOf<Method>()
            val rrs = errs.right().get().toSortedSet(ReachabilityResultComparator)
                    .filter {
                        val m = it.to
                        if (rrss.contains(m)) {
                            false
                        } else {
                            rrss.add(m)
                            true
                        }
                    }

            val rs = Reachabilities(
                    start = b,
                    reachabilities = rrs
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

    private fun parseReachabilityResults(r: BufferedReader, b: Benchmark): Either<String, List<ReachabilityResult>> {
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

    private fun parseReachabilityResult(from: Method, benchLevel: Int, l: String): Either<String, ReachabilityResult> {
        val stackDepth = parseStackDepth(l)
        val eb = parseBench(l.substringAfterLast("]").substringBefore("="))
        return if (eb.isLeft()) {
            Either.left(eb.left().get())
        } else {
            Either.right(RF.reachable(
                    from = from,
                    to = eb.right().get(),
                    level = stackDepth - benchLevel
            ))
        }
    }

    private fun parseBench(l: String): Either<String, Method> {
        val errStr = "Could not parse benchmark from '$l'"
        val cmArr = l.split(":")
        if (cmArr.size != 2) {
            return Either.left(errStr)
        }
        val c = cmArr[0]
        val mArr = cmArr[1].substring(0, cmArr[1].length - 1).split("(")
        if (mArr.size != 2) {
            return Either.left(errStr)
        }
        val m = mArr[0]
        val ps: List<String> = if (mArr[1].isBlank()) {
            listOf()
        } else {
            mArr[1].splitToSequence(",").toList()
        }

        return Either.right(MF.plainMethod(
                clazz = c,
                name = m,
                params = ps
        ))
    }

    private fun calltraceBench(b: Benchmark): String = "${b.clazz}:${b.name}(${b.params.joinToString(",")})"

    private fun cmdStr(jar: Path, b: Benchmark): String =
            if (b.jmhParams.isEmpty()) {
                String.format(cmd, jar, jcgAgentJar, inclusionsString, benchName(b))
            } else {
                String.format(cmdParam, jar, jcgAgentJar, inclusionsString, jmhParams(b), benchName(b))
            }

    private fun inclusions(): String =
            when (inclusion) {
                is IncludeAll -> ".*"
                is IncludeOnly -> inclusion.includes.joinToString(separator = ",") { "$it.*" }
            }

    private fun benchName(b: Benchmark): String = "${b.clazz.replace("$", ".")}.${b.name}"

    private fun jmhParams(b: Benchmark): String =
            b.jmhParams.joinToString(separator = ";") { "${it.first}=${it.second}" }

    companion object {
        val log = LogManager.getLogger(JavaCallgraphDCG::class.java.canonicalName)

        // arguments:
        //   1. benchmark jar path
        //   2. java-callgraph agent jar path
        //   3. call graph inclusions
        //   4. JMH parameter string (required for cmdParam and optional for cmd)
        //   5. JMH benchmark
        private const val baseCmd = "java -jar %s -jvmArgs -javaagent:%s=\"incl=%s\" -wi 0 -i 1 -f 1 -r 1 -w 1 -bm ss"
        private const val cmd = "$baseCmd %s"
        private const val cmdParam = "$baseCmd -p %s %s"

        private val jcgAgentJar = "jcg_agent.jar".fileResource().absolutePath
        private const val calltraceFileName = "calltrace.txt"

        private const val tmpDirPrefix = "bencher-java-callgraph-"
    }
}
