package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchIterationAnnotation
import org.eclipse.jdt.core.dom.MemberValuePair
import org.eclipse.jdt.core.dom.NormalAnnotation
import org.eclipse.jdt.core.dom.QualifiedName
import org.eclipse.jdt.core.dom.SingleMemberAnnotation

class JdtBenchIterationAnnotationVisitor : ASTVisitorExtended() {

    val benchIterationAnnotation = BenchIterationAnnotation()

    override fun visit(node: NormalAnnotation): Boolean {
        node.values().forEach {
            if (it is MemberValuePair) {
                val binding = it.value.resolveTypeBinding()
                if (binding.isEnum) {
                    benchIterationAnnotation.setValueEnum(it.name.identifier, BenchIterationAnnotation.bcTimeUnit, (it.value as QualifiedName).name.identifier)
                } else {
                    benchIterationAnnotation.setValue(it.name.identifier, it.value.resolveConstantExpressionValue())
                }
            }
        }
        return super.visit(node)
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        benchIterationAnnotation.setValue(null, node.value.resolveConstantExpressionValue())
        return super.visit(node)
    }
}
