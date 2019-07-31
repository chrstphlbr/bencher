package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchForkAnnotation
import org.eclipse.jdt.core.dom.MemberValuePair
import org.eclipse.jdt.core.dom.NormalAnnotation
import org.eclipse.jdt.core.dom.SingleMemberAnnotation

class JdtBenchForkAnnotationVisitor : ASTVisitorExtended() {

    val benchForkAnnotation = BenchForkAnnotation()

    override fun visit(node: NormalAnnotation): Boolean {
        node.values().forEach {
            if (it is MemberValuePair) {
                benchForkAnnotation.setValue(it.name.identifier, it.value.resolveConstantExpressionValue())
            }
        }
        return super.visit(node)
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        benchForkAnnotation.setValue(null, node.value.resolveConstantExpressionValue())
        return super.visit(node)
    }
}