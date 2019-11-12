package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.jdt.ExpressionHelper.convertToAny
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchIterationAnnotation
import org.eclipse.jdt.core.dom.*

class JdtBenchIterationAnnotationVisitor : ASTVisitor() {
    val benchIterationAnnotation = BenchIterationAnnotation()

    override fun visit(node: NormalAnnotation): Boolean {
        node.values().forEach {
            if (it is MemberValuePair) {
                if (it.value is Name) {
                    val enumValue = if (it.value is QualifiedName) {
                        (it.value as QualifiedName).name.identifier
                    } else {
                        (it.value as Name).fullyQualifiedName
                    }
                    benchIterationAnnotation.setValueEnum(it.name.identifier, BenchIterationAnnotation.bcTimeUnit, enumValue)
                } else if (it.value !is ArrayInitializer) {
                    benchIterationAnnotation.setValue(it.name.identifier, convertToAny(it.value))
                }
            }
        }
        return false
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        if (node.value !is ArrayInitializer) {
            benchIterationAnnotation.setValue(null, convertToAny(node.value))
        }
        return false
    }
}
