package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn

import arrow.core.Either
import arrow.core.getOrHandle
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.Coverages
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.*
import ch.uzh.ifi.seal.bencher.analysis.finder.MethodFinder
import ch.uzh.ifi.seal.bencher.runCommand
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileReader
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

    override fun get(jar: Path): Either<String, Coverages> {
        val ebs = benchmarkFinder.all()
        val bs: List<Benchmark> = ebs.getOrHandle {
            return Either.Left(it)
        }

        val total = bs.size
        log.info("start generating CGs")
        val startCGS = LocalDateTime.now()

        val cgs: Map<Method, Coverage> = bs.mapIndexed { i, b ->
            val l = logTimes(b, i, total, "CG for")
            val ecov = coverages(jar, b)
            l()
            ecov.getOrHandle {
                return Either.Left(it)
            }
        }.flatten().toMap()

        val endCGS = LocalDateTime.now()
        log.info("finished generating CGs in ${Duration.between(startCGS, endCGS).nano}")

        return Either.Right(Coverages(cgs))
    }

    private fun coverages(jar: Path, b: Benchmark): Either<String, List<Pair<Benchmark, Coverage>>> {
        val bs = b.parameterizedBenchmarks()

        val p = Files.createTempDirectory(tmpDirPrefix)
        val tmpDir = File(p.toUri())

        val total = bs.size
        if (total < 1) {
            return Either.Left("Expected at least 1 benchmark but was $total")
        }

        try {
            val ret: List<Pair<Benchmark, Coverage>> = if (oneCoverageForParameterizedBenchmarks) {
                val benchPair = coverageBench(jar, 0, total, tmpDir, bs[0])
                if (benchPair == null) {
                    listOf()
                } else {
                    listOf(Pair(b, replaceFrom(b, benchPair.second)))
                }
            } else {
                coverageParam(jar, total, tmpDir, bs)
            }
            return Either.Right(ret)
        } finally {
            JarHelper.deleteTmpDir(tmpDir)
        }
    }

    private fun replaceFrom(b: Benchmark, rs: Coverage): Coverage =
            b.toPlainMethod().let { pb ->
                Coverage(
                        of = b,
                        unitResults = rs.all().mapNotNull {
                            when (it) {
                                is NotCovered -> null
                                is PossiblyCovered -> CUF.possiblyCovered(
                                        of = pb,
                                        unit = it.unit,
                                        level = it.level,
                                        probability = it.probability
                                )
                                is Covered -> CUF.covered(
                                        of = pb,
                                        unit = it.unit,
                                        level = it.level
                                )
                            }
                        }.toSet()
                )
            }

    private fun coverageBench(jar: Path, i: Int, total: Int, tmpDir: File, b: Benchmark): Pair<Benchmark, Coverage>? {
        val cs = cmdStr(jar, b)

        log.debug("Param bench $b: ${i + 1}/$total; '$cs'")

        val l = logTimesParam(b, i, total, "CG for parameterized benchmark")
        val ers = exec(cs, jar, tmpDir, b)
        return try {
            ers
                .mapLeft {
                    log.error("Could not retrieve DCG for $b with '$cs': $it")
                    null
                }
                .map {
                    Pair(b, it)
                }
                .orNull()
        } finally {
            l()
        }
    }

    private fun coverageParam(jar: Path, total: Int, tmpDir: File, bs: List<Benchmark>): List<Pair<Benchmark, Coverage>> =
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

    private fun exec(cmd: String, jar: Path, dir: File, b: Benchmark): Either<String, Coverage> {
        val (ok, out, err) = cmd.runCommand(dir, timeOut)
        if (!ok) {
            return Either.Left("Execution of '$cmd' did not finish within $timeOut")
        }

        if (out != null && out.isNotBlank()) {
            log.debug("Process out: $out")
        }

        if (err != null && err.isNotBlank()) {
            log.debug("Process err: $err")
        }

        return reachabilities(jar, dir, b)
    }

    private fun reachabilities(jar: Path, dir: File, b: Benchmark): Either<String, Coverage> {
        val resultFileName = resultFileName(b)
        val fn = "$dir${File.separator}$resultFileName"
        val f = File(fn)

        if (!f.isFile) {
            return Either.Left("Result file not a file: $fn")
        }

        if (!f.exists()) {
            return Either.Left("Result file does not exist: $fn")
        }

        val ecf = transformResultFile(jar, dir, b, f)
        val cf = ecf.getOrHandle {
            return Either.Left("Could not transform result file into coverage file: $it")
        }

        if (!cf.isFile) {
            return Either.Left("Coverage file not a file: $cf")
        }

        if (!cf.exists()) {
            return Either.Left("Coverage file does not exist: $cf")
        }

        FileReader(cf).use { fr ->
            val errs = parseReachabilityResults(fr, b)
            val rrs = errs.getOrHandle {
                return Either.Left(it)
            }

            val rrss = mutableSetOf<Method>()

            val srrs = rrs.toSortedSet(CoverageUnitResultComparator)
                .filter {
                    val m = it.unit
                    if (rrss.contains(m)) {
                        false
                    } else {
                        rrss.add(m)
                        true
                    }
                }.toSet()

            log.info("CG for $b has ${srrs.size} reachable nodes (from ${rrs.size} traces)")

            val rs = Coverage(
                of = b,
                unitResults = srrs
            )

            return Either.Right(rs)
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

    protected abstract fun parseReachabilityResults(r: Reader, b: Benchmark): Either<String, Set<CoverageUnitResult>>


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
