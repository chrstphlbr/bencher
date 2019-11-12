package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchModeAnnotation
import org.eclipse.jdt.core.dom.*

class JdtBenchModeAnnotationVisitor : ASTVisitor() {
    val benchModeAnnotation = BenchModeAnnotation()

    override fun visit(node: NormalAnnotation): Boolean {
        node.values().forEach {
            if (it is MemberValuePair) {
                if (it.value is Name) {
                    processItem(it.name.identifier, it.value as Name)
                } else if (it.value is ArrayInitializer) {
                    processItems(it.name.identifier, it.value as ArrayInitializer)
                }
            }
        }
        return false
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        if (node.value is Name) {
            processItem(null, node.value as Name)
        } else {
            processItems(null, node.value as ArrayInitializer)
        }
        return false
    }

    private fun processItem(name: String?, typeName: Name) {
        val enumValue = if (typeName is QualifiedName) {
            typeName.name.identifier
        } else {
            typeName.fullyQualifiedName
        }

        benchModeAnnotation.setValueEnum(name, BenchModeAnnotation.enum, enumValue)
    }

    private fun processItems(name: String?, items: ArrayInitializer) {
        items.expressions().forEach {
            if (it is Name) {
                processItem(name, it)
            }
        }
    }
}