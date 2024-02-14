package ch.uzh.ifi.seal.bencher.analysis.coverage.dyn

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.JarHelper
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoverageExecutor
import ch.uzh.ifi.seal.bencher.analysis.coverage.Coverages
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.*
import ch.uzh.ifi.seal.bencher.analysis.finder.MethodFinder
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
        private val javaSettings: JavaSettings,
        private val oneCoverageForParameterizedBenchmarks: Boolean = true,
        private val timeOut: Duration = Duration.ofMinutes(10),
) : CoverageExecutor {

    private val env: Map<String, String> = mapOfNotNull(javaSettings.homePair())

    override fun get(jar: Path): Either<String, Coverages> {
        val ebs = benchmarkFinder.all()
        val bs: List<Benchmark> = ebs.getOrElse {
            return Either.Left(it)
        }

        val total = bs.size
        log.info("start generating coverages")
        val startCoverages = LocalDateTime.now()

        val covs: Map<Method, Coverage> = bs.mapIndexed { i, b ->
            val l = logTimes(b, i, total, "Coverages for")
            val ecov = coverages(jar, b)
            l()
            ecov.getOrElse {
                return Either.Left(it)
            }
        }.flatten().toMap()

        val endCoverages = LocalDateTime.now()
        log.info("finished generating coverages in ${Duration.between(startCoverages, endCoverages).nano}")

        return Either.Right(Coverages(covs))
    }

    private fun coverages(jar: Path, b: Benchmark): Either<String, List<Pair<Benchmark, Coverage>>> {
        val bs = b.parameterizedBenchmarks()

        val p = Files.createTempDirectory(TMP_DIR_PREFIX)
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
                                probability = it.probability,
                        )
                        is Covered -> CUF.covered(
                                of = pb,
                                unit = it.unit,
                                level = it.level,
                        )
                    }
                }.toSet(),
            )
        }

    private fun coverageBench(jar: Path, i: Int, total: Int, tmpDir: File, b: Benchmark): Pair<Benchmark, Coverage>? {
        val cs = cmdStr(jar, b)

        log.info(cs)

        log.debug("Param bench $b: ${i + 1}/$total; '$cs'")

        val l = logTimesParam(b, i, total, "coverage for parameterized benchmark")
        val ers = exec(cs, env, jar, tmpDir, b)
        return try {
            ers
                .mapLeft {
                    log.error("Could not retrieve DC for $b with '$cs': $it")
                    null
                }
                .map {
                    Pair(b, it)
                }
                .getOrNull()
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

    private fun exec(cmd: String, env: Map<String, String>, jar: Path, dir: File, b: Benchmark): Either<String, Coverage> {
        val (ok, out, err) = cmd.runCommand(dir, timeOut, env)
        if (!ok) {
            return Either.Left("Execution of '$cmd' did not finish within $timeOut")
        }

        if (!out.isNullOrBlank()) {
            log.debug("Process out: $out")
        }

        if (!err.isNullOrBlank()) {
            log.debug("Process err: $err")
        }

        return coverage(jar, dir, b)
    }

    private fun coverage(jar: Path, dir: File, b: Benchmark): Either<String, Coverage> {
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
        val cf = ecf.getOrElse {
            return Either.Left("Could not transform result file into coverage file: $it")
        }

        if (!cf.isFile) {
            return Either.Left("Coverage file not a file: $cf")
        }

        if (!cf.exists()) {
            return Either.Left("Coverage file does not exist: $cf")
        }

        FileReader(cf).use { fr ->
            val covUnitResults = parseCoverageUnitResults(fr, b).getOrElse {
                return Either.Left(it)
            }

            val newCovUnitResults = mutableSetOf<CoverageUnit>()

            val newCovUnitResultsSorted = covUnitResults.toSortedSet(CoverageUnitResultComparator)
                .filter {
                    val unit = it.unit
                    if (newCovUnitResults.contains(unit)) {
                        false
                    } else {
                        newCovUnitResults.add(unit)
                        true
                    }
                }.toSet()

            log.info("Coverage for $b has ${newCovUnitResultsSorted.size} covered units (from ${covUnitResults.size} traces)")

            val rs = Coverage(
                of = b,
                unitResults = newCovUnitResultsSorted
            )

            return Either.Right(rs)
        }
    }

    // For use in shell (e.g., bash) the constructed command string needs escaping of
    // (1) dollar-signs ($) and
    // (2) double-quotes (")
    // around the -jvmArgs argument and the JMH benchmark regex
    private fun cmdStr(jar: Path, b: Benchmark): String {
        val cmdList = mutableListOf<String>()
        // java
        cmdList.add(JAVA_CMD)

        // JVM arguments to java command
        javaSettings.jvmArgs?.let { cmdList.add(it) }

        // -jar jar
        cmdList.add(JAR_ARG)
        cmdList.add(jar.toString())

        // -jvmArgs=jvmArgs(b)
        val jmhJvmArgs = jvmArgs(b)
        javaSettings.jvmArgs?.let { cmdList.add(String.format(JMH_JVM_ARGS, it)) } // add JVM arguments also to JMH fork JVMs
        if (jmhJvmArgs.isNotEmpty()) {
            cmdList.add(String.format(JMH_JVM_ARGS, jmhJvmArgs))
        }

        // benchmark parameters
        if (b.jmhParams.isNotEmpty()) {
            cmdList.add(jmhParams(b))
        }

        // benchmark
        cmdList.add(String.format(JMH_BENCH, benchName(b)))

        // JMH arguments
        cmdList.add(JMH_ARGS)

        return cmdList.joinToString(" ")
    }

    private fun jmhParams(b: Benchmark): String =
        b.jmhParams.joinToString(separator = " ") { "-p ${it.first}=${it.second}" }

    private fun benchName(b: Benchmark): String = "${b.clazz.replace("$", ".")}.${b.name}"

    protected abstract fun jvmArgs(b: Benchmark): String

    protected abstract fun resultFileName(b: Benchmark): String

    protected abstract fun transformResultFile(jar: Path, dir: File, b: Benchmark, resultFile: File): Either<String, File>

    protected abstract fun parseCoverageUnitResults(r: Reader, b: Benchmark): Either<String, Set<CoverageUnitResult>>


    companion object {
        private val log: Logger = LogManager.getLogger(AbstractDynamicCoverage::class.java.canonicalName)

        private const val JAVA_CMD = "java"
        private const val JAR_ARG = "-jar"
        private const val JMH_JVM_ARGS = "-jvmArgs=%s"
        private const val JMH_ARGS = "-bm ss -wi 0 -i 1 -f 1 -r 1 -w 1"
        private const val JMH_BENCH = "^%s\$"

        private const val TMP_DIR_PREFIX = "bencher-dyn-cov-"
    }
}
