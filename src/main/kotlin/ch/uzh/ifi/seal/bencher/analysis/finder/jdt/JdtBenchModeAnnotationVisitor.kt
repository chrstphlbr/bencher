package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchModeAnnotation
import org.eclipse.jdt.core.dom.*

class JdtBenchModeAnnotationVisitor : ASTVisitorExtended() {
    val benchModeAnnotation = BenchModeAnnotation()

    // TODO remove
    fun mode(): List<String> = benchModeAnnotation.mode()

    override fun visit(node: NormalAnnotation): Boolean {
        node.values().forEach {
            if (it is MemberValuePair) {
                val binding = it.value.resolveTypeBinding()
                if (binding.isEnum) {
                    processItem(it.name.identifier, it.value as QualifiedName)
                } else if (binding.isArray) {
                    processItems(it.name.identifier, it.value as ArrayInitializer)
                }
            }
        }
        return super.visit(node)
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        if (node.value is QualifiedName) {
            processItem(null, node.value as QualifiedName)
        } else {
            processItems(null, node.value as ArrayInitializer)
        }

        return super.visit(node)
    }

    private fun processItem(name: String?, qualifiedName: QualifiedName) {
        benchModeAnnotation.setValueEnum(name, BenchModeAnnotation.enum, qualifiedName.name.identifier)
    }

    private fun processItems(name: String?, items: ArrayInitializer) {
        items.expressions().forEach {
            if (it is QualifiedName) {
                processItem(name, it)
            }
        }
    }
}