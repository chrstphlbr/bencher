package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.jdt.ExpressionHelper.convertToAny
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchForkAnnotation
import org.eclipse.jdt.core.dom.*

class JdtBenchForkAnnotationVisitor : ASTVisitor() {
    val benchForkAnnotation = BenchForkAnnotation()

    override fun visit(node: NormalAnnotation): Boolean {
        node.values().forEach {
            if (it is MemberValuePair && it.value !is ArrayInitializer) {
                benchForkAnnotation.setValue(it.name.identifier, convertToAny(it.value))
            }
        }
        return false
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        if (node.value !is ArrayInitializer) {
            benchForkAnnotation.setValue(null, convertToAny(node.value))
        }
        return false
    }
}