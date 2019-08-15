package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchClass
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.TypeDeclaration

class JdtBenchClassFinder(private val action: (node: TypeDeclaration, cvs: MutableList<JdtBenchClassVisitor>) -> Unit) : ASTVisitor() {
    private val cvs = mutableListOf<JdtBenchClassVisitor>()

    fun benchClass(): Set<Pair<String, BenchClass>> = cvs.map { it.benchClass() }.filterNotNull().toSet()
    fun benchs(): Set<Benchmark> = cvs.map { it.benchs() }.flatten().toSet()

    override fun visit(node: TypeDeclaration): Boolean {
        action(node, cvs)
        return super.visit(node)
    }
}