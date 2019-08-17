package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

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
                val binding = it.resolveTypeBinding()
                if (binding == null) {
                    log.warn("Fully qualified name resolution of annotation '${it.typeName.fullyQualifiedName}' cannot be resolved!")
                    return@forEach
                }
                val name = binding.qualifiedName

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
                val binding = it.value.resolveTypeBinding()
                if (binding.isArray) {
                    processItems(it.name.identifier, it.value as ArrayInitializer)
                } else {
                    processItem(it.name.identifier, it.value.resolveConstantExpressionValue() as String)
                }
            }
        }
        return super.visit(node)
    }


    override fun visit(node: SingleMemberAnnotation): Boolean {
        if (node.value is StringLiteral) {
            processItem(null, node.value.resolveConstantExpressionValue() as String)
        } else {
            processItems(null, node.value as ArrayInitializer)
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
                processItem(name, it.resolveConstantExpressionValue() as String)
            }
        }
    }
}