package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchField
import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.Annotation

class JdtBenchFieldVisitor : ASTVisitor() {
    private lateinit var fieldName: String
    val benchField = BenchField()

    private val pavs = mutableListOf<JdtBenchParamAnnotationVisitor>()

    override fun visit(node: VariableDeclarationFragment): Boolean {
        fieldName = node.name.identifier
        return false
    }

    override fun visit(node: NormalAnnotation): Boolean {
        visitAnnotation(node)
        return false
    }

    override fun visit(node: MarkerAnnotation): Boolean {
        visitAnnotation(node)
        return false
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        visitAnnotation(node)
        return false
    }

    fun visitAnnotation(node: Annotation) {
        val name = FullyQualifiedNameHelper.get(node)

        if (name == JMHConstants.Annotation.param) {
            benchField.isParam = true
            val pav = JdtBenchParamAnnotationVisitor()
            node.accept(pav)
            pavs.add(pav)
        }
    }

    override fun endVisit(node: VariableDeclarationFragment) {
        pavs.forEach { pav ->
            benchField.jmhParams[fieldName] = pav.jmhParams
        }
    }
}