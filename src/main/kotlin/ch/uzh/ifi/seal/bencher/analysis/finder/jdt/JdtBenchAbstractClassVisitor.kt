package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchClass
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.StateObjectManager
import org.eclipse.jdt.core.dom.ASTVisitor
import org.eclipse.jdt.core.dom.Annotation
import org.eclipse.jdt.core.dom.FieldDeclaration
import org.eclipse.jdt.core.dom.TypeDeclaration

abstract class JdtBenchAbstractClassVisitor(som: StateObjectManager? = null) : ASTVisitor() {
    protected lateinit var fullyQualifiedClassName: String
    protected val benchClass = BenchClass(som)

    // sub visitor
    protected val fvs: MutableList<JdtBenchFieldVisitor> = mutableListOf()

    protected fun isFqnInit() = ::fullyQualifiedClassName.isInitialized

    override fun visit(node: TypeDeclaration): Boolean {
        fullyQualifiedClassName = FullyQualifiedNameHelper.getClassName(node)

        node.fields.forEach {
            visitField(it)
        }

        node.modifiers().forEach {
            if (it is Annotation) {
                visitAnnotation(it)
            }
        }

        val bf = fvs.map { it.benchField }
        benchClass.setJmhParams(bf)

        return false
    }


    private fun visitField(node: FieldDeclaration) {
        val fv = JdtBenchFieldVisitor()
        node.accept(fv)
        fvs.add(fv)
    }

    abstract fun visitAnnotation(node: Annotation)
}