package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.Annotation

open class ASTVisitorExtended : ASTVisitor() {
    fun visit(node: Annotation) {
        when (node) {
            is NormalAnnotation -> visit(node)
            is MarkerAnnotation -> visit(node)
            is SingleMemberAnnotation -> visit(node)
        }
    }
}