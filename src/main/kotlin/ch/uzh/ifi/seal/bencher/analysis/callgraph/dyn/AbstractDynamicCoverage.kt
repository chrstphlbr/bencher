package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.*
import ch.uzh.ifi.seal.bencher.analysis.finder.MethodFinder
import ch.uzh.ifi.seal.bencher.runCommand
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.funktionale.either.Either
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDateTime

abstract class AbstractDynamicCoverage(
        private val benchmarkFinder: MethodFinder<Benchmark>,
        private val oneCoverageForParameterizedBenchmarks: Boolean = true,
        private val timeOut: Duration = Duration.ofMinutes(10)
) : CGExecutor {

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
            val ecov = coverages(jar, b)
            l()
            if (ecov.isLeft()) {
                return Either.left(ecov.left().get())
            }
            ecov.right().get()
        }.flatten().toMap()

        val endCGS = LocalDateTime.now()
        log.info("finished generating CGs in ${Duration.between(startCGS, endCGS).nano}")

        return Either.right(CGResult(cgs))
    }

    private fun coverages(jar: Path, b: Benchmark): Either<String, List<Pair<Benchmark, Reachabilities>>> {
        val bs = b.parameterizedBenchmarks()

        val p = Files.createTempDirectory(tmpDirPrefix)
        val tmpDir = File(p.toUri())

        val total = bs.size
        if (total < 1) {
            return Either.left("Expected at least 1 benchmark but was $total")
        }

        try {
            val ret: List<Pair<Benchmark, Reachabilities>> = if (oneCoverageForParameterizedBenchmarks) {
                val benchPair = coverageBench(jar, 0, total, tmpDir, bs[0])
                if (benchPair == null) {
                    listOf()
                } else {
                    listOf(Pair(b, replaceFrom(b, benchPair.second)))
                }
            } else {
                coverageParam(jar, total, tmpDir, bs)
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

    private fun coverageBench(jar: Path, i: Int, total: Int, tmpDir: File, b: Benchmark): Pair<Benchmark, Reachabilities>? {
        val cs = cmdStr(jar, b)

        log.debug("Param bench $b: ${i + 1}/$total; '$cs'")

        val l = logTimesParam(b, i, total, "CG for parameterized benchmark")
        val ers = exec(cs, jar, tmpDir, b)
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

    private fun coverageParam(jar: Path, total: Int, tmpDir: File, bs: List<Benchmark>): List<Pair<Benchmark, Reachabilities>> =
            bs.mapIndexedNotNull { i, pb -> coverageBench(jar, i, total, tmpDir, pb) }

    private fun logTimesParam(b: Benchmark, i: Int, total: Int, text: String): () -> Unit =
            if (total <= 1) {
                {}
            } else {
                logTimes(b, i, total, text)
            }

    private fun logTimes(b: Benchmark, i: Int, total: Int, text: String): () -> Unit {
        log.info("start $text $b (${i + 1}/$total)")
        val start = System.nanoTime()
        return {
            val dur = System.nanoTime() - start
            log.info("finished $text $b (${i + 1}/$total) in ${dur}ns")
        }
    }

    private fun exec(cmd: String, jar: Path, dir: File, b: Benchmark): Either<String, Reachabilities> {
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

        return reachabilities(jar, dir, b)
    }

    private fun reachabilities(jar: Path, dir: File, b: Benchmark): Either<String, Reachabilities> {
        val resultFileName = resultFileName(b)
        val fn = "$dir${File.separator}$resultFileName"
        val f = File(fn)

        if (!f.isFile) {
            return Either.left("Not a file: $fn")
        }

        if (!f.exists()) {
            return Either.left("File does not exist: $fn")
        }

        val ecv = transformResultFile(jar, dir, b, f)
        if (ecv.isLeft()) {
            return Either.left("Could not get coverage file: ${ecv.left()}")
        }

        val fr = FileReader(ecv.right().get())

        try {
            val errs = parseReachabilityResults(fr, b)
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

    private fun cmdStr(jar: Path, b: Benchmark): String =
            if (b.jmhParams.isEmpty()) {
                String.format(cmd, jar, jvmArgs(b), benchName(b))
            } else {
                String.format(cmdParam, jar, jvmArgs(b), jmhParams(b), benchName(b))
            }

    private fun jmhParams(b: Benchmark): String =
            b.jmhParams.joinToString(separator = " ") { "-p ${it.first}=${it.second}" }

    private fun benchName(b: Benchmark): String = "${b.clazz.replace("$", ".")}.${b.name}"

    protected abstract fun jvmArgs(b: Benchmark): String

    protected abstract fun resultFileName(b: Benchmark): String

    protected abstract fun transformResultFile(jar: Path, dir: File, b: Benchmark, resultFile: File): Either<String, File>

    protected abstract fun parseReachabilityResults(r: Reader, b: Benchmark): Either<String, Set<ReachabilityResult>>


    companion object {
        val log: Logger = LogManager.getLogger(AbstractDynamicCoverage::class.java.canonicalName)

        // arguments:
        // For use in shell (e.g., bash) the constructed command string needs escaping of
        // (1) dollar-signs ($) and
        // (2) double-quotes (")
        // around the -jvmArgs argument and the JMH benchmark regex
        //   1. benchmark jar path
        //   2. JVM args string
        //   3. JMH parameter string (required for cmdParam and optional for cmd)
        //   4. JMH benchmark
        private const val baseCmd = "java -jar %s -jvmArgs=%s -bm ss -wi 0 -i 1 -f 1 -r 1 -w 1"
        private const val cmd = "$baseCmd ^%s\$"
        private const val cmdParam = "$baseCmd %s ^%s\$"

        private const val tmpDirPrefix = "bencher-dyn-cov-"
    }
}
