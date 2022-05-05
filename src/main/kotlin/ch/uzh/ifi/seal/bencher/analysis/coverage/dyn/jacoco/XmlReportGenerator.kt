package ch.uzh.ifi.seal.bencher.analysis.coverage.dyn.jacoco

import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.IBundleCoverage
import org.jacoco.core.analysis.IClassCoverage
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.IReportVisitor
import org.jacoco.report.ISourceFileLocator
import org.jacoco.report.MultiSourceFileLocator
import org.jacoco.report.xml.XMLFormatter
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.PrintWriter

// XmlReportGenerator works like Report (copied implementation from https://github.com/jacoco/jacoco and slightly adjusted)
// https://github.com/jacoco/jacoco/blob/master/org.jacoco.cli/src/org/jacoco/cli/internal/commands/Report.java
internal object XmlReportGenerator {

    private const val tabwidth = 4
    private const val name = "JaCoCo Coverage Report"

    @Throws(IOException::class)
    fun execute(execfiles: List<File>, classfiles: List<File>, reportOut: OutputStream, out: PrintWriter, err: PrintWriter) {
        val loader = loadExecutionData(execfiles = execfiles, out = out)
        val bundle = analyze(data = loader.executionDataStore, classfiles = classfiles, out = out)
        writeReports(bundle = bundle, loader = loader, reportOut = reportOut, out = out)
    }

    @Throws(IOException::class)
    private fun loadExecutionData(execfiles: List<File>, out: PrintWriter): ExecFileLoader {
        val loader = ExecFileLoader()
        if (execfiles.isEmpty()) {
            out.println("[WARN] No execution data files provided.")
        } else {
            for (file in execfiles) {
                out.printf("[INFO] Loading execution data file %s.%n", file.getAbsolutePath())
                loader.load(file)
            }
        }
        return loader
    }

    @Throws(IOException::class)
    private fun analyze(data: ExecutionDataStore, classfiles: List<File>, out: PrintWriter): IBundleCoverage? {
        val builder = DuplicateCoverageBuilder()
        val analyzer = Analyzer(data, builder)
        for (f in classfiles) {
            analyzer.analyzeAll(f)
        }
        builder.noMatchClasses?.let { printNoMatchWarning(it, out) }
        return builder.getBundle(name)
    }

    private fun printNoMatchWarning(nomatch: Collection<IClassCoverage>, out: PrintWriter) {
        if (!nomatch.isEmpty()) {
            out.println("[WARN] Some classes do not match with execution data.")
            out.println("[WARN] For report generation the same class files must be used as at runtime.")
            for (c in nomatch) {
                out.printf("[WARN] Execution data for class %s does not match.%n", c.name)
            }
        }
    }

    @Throws(IOException::class)
    private fun writeReports(bundle: IBundleCoverage?, loader: ExecFileLoader, reportOut: OutputStream, out: PrintWriter) {
        if (bundle != null) {
            out.printf("[INFO] Analyzing %s classes.%n", Integer.valueOf(bundle.classCounter.totalCount))
        }
        val visitor: IReportVisitor = createReportVisitor(reportOut)
        visitor.visitInfo(loader.sessionInfoStore.infos, loader.executionDataStore.contents)
        visitor.visitBundle(bundle, getSourceLocator())
        visitor.visitEnd()
    }

    @Throws(IOException::class)
    private fun createReportVisitor(reportOut: OutputStream): IReportVisitor = XMLFormatter().createVisitor(reportOut)

    private fun getSourceLocator(): ISourceFileLocator = MultiSourceFileLocator(tabwidth)
}