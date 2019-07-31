package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.Benchmark
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.TypeDeclaration

class JdtBenchClassFinder : ASTVisitor() {
    private val cvs = mutableListOf<JdtBenchClassVisitor>()

    fun benchs(): Set<Benchmark> = cvs.map { it.benchs() }.flatten().toSet()

    override fun visit(node: TypeDeclaration): Boolean {
        val cv = JdtBenchClassVisitor()

        cv.visit(node)
        cvs.add(cv)
        return super.visit(node)
    }
}