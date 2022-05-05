package ch.uzh.ifi.seal.bencher.analysis.coverage.dyn.jacoco

import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.analysis.IBundleCoverage
import org.jacoco.core.analysis.IClassCoverage
import org.jacoco.core.analysis.ISourceFileCoverage
import org.jacoco.core.internal.analysis.BundleCoverageImpl
import org.jacoco.core.internal.analysis.SourceFileCoverageImpl
import java.util.*

// DuplicateCoverageBuilder works like CoverageBuilder (also copied implementation from https://github.com/jacoco/jacoco and only adjusted visitCoverage)
// but allows for duplicate classes (as potentially coming from JAR files)
// https://github.com/jacoco/jacoco/blob/master/org.jacoco.core/src/org/jacoco/core/analysis/CoverageBuilder.java
internal class DuplicateCoverageBuilder : CoverageBuilder() {

    private val classes: MutableMap<String, IClassCoverage> = HashMap()
    private val sourcefiles: MutableMap<String, ISourceFileCoverage> = HashMap()

    /**
     * Returns all class nodes currently contained in this builder.
     *
     * @return all class nodes
     */
    override fun getClasses(): Collection<IClassCoverage?>? {
        return Collections.unmodifiableCollection(classes.values)
    }

    /**
     * Returns all source file nodes currently contained in this builder.
     *
     * @return all source file nodes
     */
    override fun getSourceFiles(): Collection<ISourceFileCoverage?>? {
        return Collections.unmodifiableCollection(sourcefiles.values)
    }

    /**
     * Creates a bundle from all nodes currently contained in this bundle.
     *
     * @param name
     * Name of the bundle
     * @return bundle containing all classes and source files
     */
    override fun getBundle(name: String?): IBundleCoverage? {
        return BundleCoverageImpl(name, classes.values, sourcefiles.values)
    }

    /**
     * Returns all classes for which execution data does not match.
     *
     * @see IClassCoverage.isNoMatch
     * @return collection of classes with non-matching execution data
     */
    override fun getNoMatchClasses(): Collection<IClassCoverage>? {
        val result: MutableCollection<IClassCoverage> = ArrayList()
        for (c in classes.values) {
            if (c.isNoMatch) {
                result.add(c)
            }
        }
        return result
    }

    // === ICoverageVisitor ===

    // === ICoverageVisitor ===
    override fun visitCoverage(coverage: IClassCoverage) {
        val name = coverage.name
        val cov = classes[name]
        if (cov != null) {
            // this line is the only one that changed
            // do not throw IllegalStateException but just return
            // and only keep the first encountered coverage information
            return
        } else {
            classes[name] = coverage
            val source = coverage.sourceFileName
            if (source != null) {
                val sourceFile = getSourceFile(source,
                        coverage.packageName)
                sourceFile.increment(coverage)
            }
        }
    }

    private fun getSourceFile(filename: String,
                              packagename: String): SourceFileCoverageImpl {
        val key = "$packagename/$filename"
        var sourcefile = sourcefiles[key] as SourceFileCoverageImpl?
        if (sourcefile == null) {
            sourcefile = SourceFileCoverageImpl(filename, packagename)
            sourcefiles[key] = sourcefile
        }
        return sourcefile
    }
}
