package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchOutputTimeUnitAnnotation
import org.eclipse.jdt.core.dom.*

class JdtBenchOutputTimeUnitAnnotationVisitor : ASTVisitorExtended() {
    val benchOutputTimeUnitAnnotation = BenchOutputTimeUnitAnnotation()

    override fun visit(node: NormalAnnotation): Boolean {
        node.values().forEach {
            if (it is MemberValuePair) {
                if (it.value is Name) {
                    benchOutputTimeUnitAnnotation.setValueEnum(it.name.identifier, BenchOutputTimeUnitAnnotation.bcTimeUnit, convertEnumValue(it.value as Name))
                }
            }
        }
        return super.visit(node)
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        if (node.value is Name) {
            benchOutputTimeUnitAnnotation.setValueEnum(null, BenchOutputTimeUnitAnnotation.bcTimeUnit, convertEnumValue(node.value as Name))
        }

        return super.visit(node)
    }

    private fun convertEnumValue(name: Name): String {
        return if (name is QualifiedName) {
            name.name.identifier
        } else {
            name.fullyQualifiedName
        }
    }
}