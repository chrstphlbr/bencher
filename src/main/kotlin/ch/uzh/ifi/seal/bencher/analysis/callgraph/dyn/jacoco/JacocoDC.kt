package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn.jacoco

import arrow.core.Either
import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGInclusions
import ch.uzh.ifi.seal.bencher.analysis.callgraph.IncludeAll
import ch.uzh.ifi.seal.bencher.analysis.callgraph.IncludeOnly
import ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn.AbstractDynamicCoverage
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.CUF
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.CoverageUnitResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.computation.Covered
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

class JacocoDC(
        benchmarkFinder: MethodFinder<Benchmark>,
        oneCoverageForParameterizedBenchmarks: Boolean = true,
        inclusion: CGInclusions = IncludeAll,
        timeOut: Duration = Duration.ofMinutes(10)
) : AbstractDynamicCoverage(
        benchmarkFinder = benchmarkFinder,
        oneCoverageForParameterizedBenchmarks = oneCoverageForParameterizedBenchmarks,
        timeOut = timeOut
), CGExecutor {

    private val inclusionsString = inclusions(inclusion)

    override fun resultFileName(b: Benchmark): String = fileName(b, execFileExt)

    override fun transformResultFile(jar: Path, dir: File, b: Benchmark, resultFile: File): Either<String, File> {
        val p = Paths.get(dir.path, reportFileName)
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
                    err = PrintWriter(System.err)
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

    override fun parseReachabilityResults(r: Reader, b: Benchmark): Either<String, Set<CoverageUnitResult>> {
        val from = b.toPlainMethod()

        val xmlFac = XMLInputFactory.newInstance()
        val sr = xmlFac.createXMLStreamReader(r)

        try {
            var rs = mutableSetOf<CoverageUnitResult>()

            var className = ""
            var methodName = ""
            var desc = ""

            var state = 0

            while (sr.hasNext()) {
                when (sr.next()) {
                    XMLStreamConstants.START_ELEMENT -> {
                        when (sr.localName) {
                            xmlTagClass -> {
                                if (state == 0) {
                                    state++
                                    className = sr.getAttributeValue(null, xmlAttrName)
                                }
                            }
                            xmlTagMethod -> {
                                if (state == 1) {
                                    state++
                                    methodName = sr.getAttributeValue(null, xmlAttrName)
                                    desc = sr.getAttributeValue(null, xmlAttrDesc)
                                }
                            }
                            xmlTagCounter -> {
                                if (state == 2) {
                                    state++
                                    val type = sr.getAttributeValue(null, xmlAttrType)
                                    if (type == xmlAttrTypeMethod && state == 3) {
                                        val covered = sr.getAttributeValue(null, xmlAttrCovered)
                                        if (covered == "1") {
                                            rs.add(reachabilitResult(from, className, methodName, desc))
                                        }
                                    }
                                }
                            }
                        }

                    }
                    XMLStreamConstants.END_ELEMENT -> {
                        when (sr.localName) {
                            xmlTagClass -> {
                                if (state == 1) {
                                    state--
                                    className = ""
                                }
                            }
                            xmlTagMethod -> {
                                if (state == 2) {
                                    state--
                                    methodName = ""
                                    desc = ""
                                }
                            }
                            xmlTagCounter -> {
                                if (state == 3) {
                                    state--
                                }
                            }
                        }
                    }
                }
            }

            return Either.Right(rs)
        } finally {
            sr.close()
        }
    }

    private fun reachabilitResult(from: Method, c: String, m: String, d: String): Covered {
        val params: List<String> = descriptorToParamList(d).getOrElse { listOf() }

        val ret: String = descriptorToReturnType(d).getOrElse { SourceCodeConstants.void }

        return CUF.covered(
                of = from,
                unit = MF.plainMethod(
                        clazz = c.replaceSlashesWithDots,
                        name = m,
                        params = params,
                        returnType = ret
                ),
                level = defaultStackDepth
        )
    }

    override fun jvmArgs(b: Benchmark): String =
            String.format(jvmArgs, agentJar, fileName(b, execFileExt), inclusionsString, exclusionsString)

    private fun inclusions(i: CGInclusions): String =
            when (i) {
                is IncludeAll -> "*"
                is IncludeOnly -> i.includes.joinToString(separator = ":") { "${it.replaceDotsWithSlashes}*" }
            }


    companion object {
        val log: Logger = LogManager.getLogger(JacocoDC::class.java.canonicalName)

        private const val execFileExt = "exec"
        private const val reportFileName = "jacoco_report.xml"

        // JVM arguments
        //   1. Jacoco agent jar path (e.g., agentJar)
        //   2. Jacoco (binary) execution file
        //   3. Jacoco inclusions (e.g., inclusionsString)
        //   4. Jacoco exclusions (e.g., exclusionsString)
        private const val jvmArgs = "-javaagent:%s=destfile=%s,includes=%s,excludes=%s"

        private val agentJar = "jacocoagent.jar.zip".fileResource().absolutePath
        private val exclusionsString: String = setOf(
                "*generated/*_jmhTest*",
                "*generated/*_jmhType*"
        ).joinToString(":")

        private const val defaultStackDepth = -1

        private const val xmlTagClass = "class"
        private const val xmlTagMethod = "method"
        private const val xmlTagCounter = "counter"
        private const val xmlAttrName = "name"
        private const val xmlAttrDesc = "desc"
        private const val xmlAttrType = "type"
        private const val xmlAttrTypeMethod = "METHOD"
        private const val xmlAttrCovered = "covered"
    }
}
