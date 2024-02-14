package ch.uzh.ifi.seal.bencher.analysis.coverage.dyn.jacoco

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoverageExecutor
import ch.uzh.ifi.seal.bencher.analysis.coverage.CoverageInclusions
import ch.uzh.ifi.seal.bencher.analysis.coverage.IncludeAll
import ch.uzh.ifi.seal.bencher.analysis.coverage.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.*
import ch.uzh.ifi.seal.bencher.analysis.coverage.dyn.AbstractDynamicCoverage
import ch.uzh.ifi.seal.bencher.analysis.descriptorToParamList
import ch.uzh.ifi.seal.bencher.analysis.descriptorToReturnType
import ch.uzh.ifi.seal.bencher.analysis.finder.MethodFinder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.*
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader

class JacocoDC(
    benchmarkFinder: MethodFinder<Benchmark>,
    javaSettings: JavaSettings,
    oneCoverageForParameterizedBenchmarks: Boolean = true,
    inclusion: CoverageInclusions = IncludeAll,
    private val coverageUnitType: CoverageUnitType,
    timeOut: Duration = Duration.ofMinutes(10)
) : AbstractDynamicCoverage(
    benchmarkFinder = benchmarkFinder,
    javaSettings = javaSettings,
    oneCoverageForParameterizedBenchmarks = oneCoverageForParameterizedBenchmarks,
    timeOut = timeOut,
), CoverageExecutor {

    private val inclusionsString = inclusions(inclusion)

    override fun resultFileName(b: Benchmark): String = fileName(b, EXEC_FILE_EXT)

    override fun transformResultFile(jar: Path, dir: File, b: Benchmark, resultFile: File): Either<String, File> {
        val p = Paths.get(dir.path, REPORT_FILE_NAME)
        val file = p.toFile()
        if (file.exists()) {
            file.delete()
        }

        return try {
            FileOutputStream(file).use { fos ->
                XmlReportGenerator.execute(
                    execfiles = listOf(resultFile),
                    classfiles = listOf(jar.toFile()),
                    reportOut = fos,
                    out = PrintWriter(System.out),
                    err = PrintWriter(System.err),
                )
                Either.Right(file)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Either.Left("Could not generate Jacoco report: ${e.message}")
        }
    }

    private fun fileName(b: Benchmark, ext: String): String {
        val sb = StringBuilder()
        val sep = "__"

        sb.append(b.clazz.replace(".", "_"))
        sb.append(sep)
        sb.append(b.name)

        if (b.jmhParams.isNotEmpty()) {
            sb.append(sep)
            sb.append(b.jmhParams.joinToString("_") { (k, v) -> "$k=$v" })
        }

        sb.append(".")
        sb.append(ext)

        return sb.toString()
    }

    override fun parseCoverageUnitResults(r: Reader, b: Benchmark): Either<String, Set<CoverageUnitResult>> {
        val of = b.toPlainMethod()

        val xmlFac = XMLInputFactory.newInstance()
        val sr = xmlFac.createXMLStreamReader(r)

        try {
            val methodCoverage = mutableSetOf<CoverageUnitResult>()
            var className = ""
            var methodName = ""
            var desc = ""

            val lineCoverage = mutableSetOf<CoverageUnitResult>()
            var fileName = ""

            var state = 0

            while (sr.hasNext()) {
                when (sr.next()) {
                    XMLStreamConstants.START_ELEMENT -> {
                        when (sr.localName) {
                            // class-/method-based counters
                            XML_TAG_CLASS -> {
                                if (state == 0) {
                                    state++
                                    className = sr.getAttributeValue(null, XML_ATTR_NAME)
                                }
                            }
                            XML_TAG_METHOD -> {
                                if (state == 1) {
                                    state++
                                    methodName = sr.getAttributeValue(null, XML_ATTR_NAME)
                                    desc = sr.getAttributeValue(null, XML_ATTR_DESC)
                                }
                            }
                            XML_TAG_COUNTER -> {
                                if (state == 2) {
                                    state++
                                    val type = sr.getAttributeValue(null, XML_ATTR_TYPE)
                                    if (type == XML_ATTR_TYPE_METHOD) {
                                        val covered = sr.getAttributeValue(null, XML_ATTR_COVERED)
                                        if (covered == "1") {
                                            methodCoverage.add(methodCovered(of, className, methodName, desc))
                                        }
                                    }
                                }
                            }

                            // file-based counters
                            XML_TAG_SOURCE_FILE -> {
                                if (state == 0) {
                                    state += 10
                                    fileName = sr.getAttributeValue(null, XML_ATTR_NAME)
                                }
                            }
                            XML_TAG_LINE -> {
                                if (state == 10) {
                                    state += 10

                                    val nr = intAttr(sr, XML_ATTR_NR).getOrElse { return Either.Left(it) }
                                    val mi =
                                        intAttr(sr, XML_ATTR_MISSED_INSTRUCTIONS).getOrElse { return Either.Left(it) }
                                    val ci =
                                        intAttr(sr, XML_ATTR_COVERED_INSTRUCTIONS).getOrElse { return Either.Left(it) }
                                    val mb = intAttr(sr, XML_ATTR_MISSED_BRANCHES).getOrElse { return Either.Left(it) }
                                    val cb = intAttr(sr, XML_ATTR_COVERED_BRANCHES).getOrElse { return Either.Left(it) }

                                    if (ci != 0 || cb != 0) {
                                        lineCoverage.add(
                                            lineCovered(
                                                of = of,
                                                f = fileName,
                                                ln = nr,
                                                mi = mi,
                                                ci = ci,
                                                mb = mb,
                                                cb = cb,
                                            )
                                        )
                                    }
                                }
                            }
                        }

                    }
                    XMLStreamConstants.END_ELEMENT -> {
                        when (sr.localName) {
                            // class-/method-based counters
                            XML_TAG_CLASS -> {
                                if (state == 1) {
                                    state--
                                    className = ""
                                }
                            }
                            XML_TAG_METHOD -> {
                                if (state == 2) {
                                    state--
                                    methodName = ""
                                    desc = ""
                                }
                            }
                            XML_TAG_COUNTER -> {
                                if (state == 3) {
                                    state--
                                }
                            }

                            // file-based counters
                            XML_TAG_SOURCE_FILE -> {
                                if (state == 10) {
                                    state -= 10
                                    fileName = ""
                                }
                            }
                            XML_TAG_LINE -> {
                                if (state == 20) {
                                    state -= 10
                                }
                            }
                        }
                    }
                }
            }

            val coverage: Set<CoverageUnitResult> = when (coverageUnitType) {
                CoverageUnitType.ALL -> methodCoverage + lineCoverage
                CoverageUnitType.LINE -> lineCoverage
                CoverageUnitType.METHOD -> methodCoverage
            }

            return Either.Right(coverage)
        } finally {
            sr.close()
        }
    }

    private fun intAttr(sr: XMLStreamReader, attr: String): Either<String, Int> {
        val str = sr.getAttributeValue(null, attr)

        return try {
            val i = str.toInt()
            Either.Right(i)
        } catch (e: NumberFormatException) {
            Either.Left("could not parse attribute '$attr': ${e.message}")
        }
    }

    private fun methodCovered(of: Method, c: String, m: String, d: String): Covered {
        val params: List<String> = descriptorToParamList(d).getOrElse { listOf() }

        val ret: String = descriptorToReturnType(d).getOrElse { SourceCodeConstants.void }

        return CUF.covered(
            of = of,
            unit = CoverageUnitMethod(
                MF.plainMethod(
                    clazz = c.replaceSlashesWithDots,
                    name = m,
                    params = params,
                    returnType = ret,
                )
            ),
            level = DEFAULT_STACK_DEPTH,
        )
    }

    private fun lineCovered(of: Method, f: String, ln: Int, mi: Int, ci: Int, mb: Int, cb: Int): Covered {
        return CUF.covered(
            of = of,
            unit = CoverageUnitLine(
                line = LF.line(
                    file = f,
                    number = ln
                ),
                missedInstructions = mi,
                coveredInstructions = ci,
                missedBranches = mb,
                coveredBranches = cb,
            ),
            level = DEFAULT_STACK_DEPTH,
        )
    }

    override fun jvmArgs(b: Benchmark): String =
        String.format(JVM_ARGS, agentJar, fileName(b, EXEC_FILE_EXT), inclusionsString, exclusionsString)

    private fun inclusions(i: CoverageInclusions): String =
        when (i) {
            is IncludeAll -> "*"
            is IncludeOnly -> i.includes.joinToString(separator = ":") { "${it.replaceDotsWithSlashes}*" }
        }


    companion object {
        private val log: Logger = LogManager.getLogger(JacocoDC::class.java.canonicalName)

        private const val EXEC_FILE_EXT = "exec"
        private const val REPORT_FILE_NAME = "jacoco_report.xml"

        // JVM arguments
        //   1. Jacoco agent jar path (e.g., agentJar)
        //   2. Jacoco (binary) execution file
        //   3. Jacoco inclusions (e.g., inclusionsString)
        //   4. Jacoco exclusions (e.g., exclusionsString)
        private const val JVM_ARGS = "-javaagent:%s=destfile=%s,includes=%s,excludes=%s"

        private val agentJar = "jacocoagent.jar.zip".fileResource().absolutePath
        private val exclusionsString: String = setOf(
                "*generated/*_jmhTest*",
                "*generated/*_jmhType*"
        ).joinToString(":")

        private const val DEFAULT_STACK_DEPTH = -1

        private const val XML_TAG_CLASS = "class"
        private const val XML_TAG_METHOD = "method"
        private const val XML_TAG_COUNTER = "counter"
        private const val XML_TAG_SOURCE_FILE = "sourcefile"
        private const val XML_TAG_LINE = "line"
        private const val XML_ATTR_NAME = "name"
        private const val XML_ATTR_DESC = "desc"
        private const val XML_ATTR_TYPE = "type"
        private const val XML_ATTR_TYPE_METHOD = "METHOD"
        private const val XML_ATTR_COVERED = "covered"
        private const val XML_ATTR_NR = "nr"
        private const val XML_ATTR_MISSED_INSTRUCTIONS = "mi"
        private const val XML_ATTR_COVERED_INSTRUCTIONS = "ci"
        private const val XML_ATTR_MISSED_BRANCHES = "mb"
        private const val XML_ATTR_COVERED_BRANCHES = "cb"
    }
}
