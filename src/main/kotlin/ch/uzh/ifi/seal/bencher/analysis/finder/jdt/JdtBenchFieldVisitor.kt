package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.jdt.ExpressionHelper.convertToAny
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchField
import org.apache.logging.log4j.LogManager
import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.Annotation

class JdtBenchFieldVisitor : ASTVisitorExtended() {
    private val log = LogManager.getLogger(JdtBenchFieldVisitor::class.java.canonicalName)

    private lateinit var fieldName: String
    val benchField = BenchField()

    override fun visit(node: FieldDeclaration): Boolean {
        node.fragments().forEach {
            if (it is VariableDeclarationFragment) {
                fieldName = it.name.identifier
            }
        }
        node.modifiers().forEach {
            if (it is Annotation) {
                val name = FullyQualifiedNameHelper.get(it)

                if (name == JMHConstants.Annotation.param) {
                    benchField.isParam = true
                    visit(it)
                }
            }
        }

        return super.visit(node)
    }

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
        return super.visit(node)
    }


    override fun visit(node: SingleMemberAnnotation): Boolean {
        if (node.value is StringLiteral) {
            processItem(null, convertToString(node.value))
        } else if (node.value is ArrayInitializer) {
            processItems(null, node.value as ArrayInitializer)
        } else {
            processItem(null, convertToString(node.value))
        }

        return super.visit(node)
    }

    private fun processItem(name: String?, value: String) {
        val list = benchField.jmhParams.getOrPut(fieldName) { mutableListOf() }
        list.add(value)
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