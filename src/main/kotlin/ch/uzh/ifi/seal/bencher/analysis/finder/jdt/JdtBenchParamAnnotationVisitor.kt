package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.jdt.ExpressionHelper.convertToAny
import org.eclipse.jdt.core.dom.*

class JdtBenchParamAnnotationVisitor : ASTVisitor() {
    val jmhParams = mutableListOf<String>()

    override fun visit(node: NormalAnnotation): Boolean {
        node.values().forEach {
            if (it is MemberValuePair) {
                if (it.value is ArrayInitializer) {
                    processItems(it.name.identifier, it.value as ArrayInitializer)
                } else {
                    processItem(it.name.identifier, convertToString(it.value))
                }
            }
        }
        return false
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        when(node.value){
            is StringLiteral -> processItem(null, convertToString(node.value))
            is ArrayInitializer -> processItems(null, node.value as ArrayInitializer)
            else -> processItem(null, convertToString(node.value))
        }

        return false
    }

    private fun processItem(name: String?, value: String) {
        jmhParams.add(value)
    }

    private fun processItems(name: String?, items: ArrayInitializer) {
        items.expressions().forEach {
            if (it is Expression) {
                processItem(name, convertToString(it))
            }
        }
    }

    private fun convertToString(expression: Expression): String {
        return convertToAny(expression) as String
    }
}