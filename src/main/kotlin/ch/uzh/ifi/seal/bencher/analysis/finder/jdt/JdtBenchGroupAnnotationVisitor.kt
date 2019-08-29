package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.jdt.ExpressionHelper.convertToAny
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchGroupAnnotation
import org.eclipse.jdt.core.dom.*

class JdtBenchGroupAnnotationVisitor : ASTVisitor() {
    val benchGroupAnnotation = BenchGroupAnnotation()

    override fun visit(node: NormalAnnotation): Boolean {
        node.values().forEach {
            if (it is MemberValuePair && it.value !is ArrayInitializer) {
                benchGroupAnnotation.setValue(it.name.identifier, convertToAny(it.value))
            }
        }
        return false
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        if (node.value !is ArrayInitializer) {
            benchGroupAnnotation.setValue(null, convertToAny(node.value))
        }
        return false
    }
}