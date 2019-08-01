package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchIterationAnnotation
import org.eclipse.jdt.core.dom.*

class JdtBenchIterationAnnotationVisitor : ASTVisitorExtended() {

    val benchIterationAnnotation = BenchIterationAnnotation()

    override fun visit(node: NormalAnnotation): Boolean {
        node.values().forEach {
            if (it is MemberValuePair) {
                val binding = it.value.resolveTypeBinding()
                if (binding.isEnum) {
                    benchIterationAnnotation.setValueEnum(it.name.identifier, BenchIterationAnnotation.bcTimeUnit, (it.value as QualifiedName).name.identifier)
                } else if (it.value !is ArrayInitializer) {
                    benchIterationAnnotation.setValue(it.name.identifier, it.value.resolveConstantExpressionValue())
                }
            }
        }
        return super.visit(node)
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        if (node.value !is ArrayInitializer) {
            benchIterationAnnotation.setValue(null, node.value.resolveConstantExpressionValue())
        }
        return super.visit(node)
    }
}
