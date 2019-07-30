package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.TypeDeclaration

class JdtBenchClassFinder : ASTVisitor() {
    private val cvs = mutableListOf<JdtBenchClassVisitor>()

    override fun visit(node: TypeDeclaration): Boolean {
        val cv = JdtBenchClassVisitor()

        cv.visit(node)
        cvs.add(cv)
        return super.visit(node)
    }
}