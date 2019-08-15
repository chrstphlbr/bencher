package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchClass
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.StateObjectManager
import org.apache.logging.log4j.LogManager
import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.Annotation

abstract class JdtBenchAbstractClassVisitor(som: StateObjectManager? = null) : ASTVisitorExtended() {
    private val log = LogManager.getLogger(JdtBenchAbstractClassVisitor::class.java.canonicalName)

    protected lateinit var fullyQualifiedClassName: String
    protected val benchClass = BenchClass(som)

    // sub visitor
    protected val fvs: MutableList<JdtBenchFieldVisitor> = mutableListOf()

    protected fun isFqnInit() = ::fullyQualifiedClassName.isInitialized

    override fun visit(node: TypeDeclaration): Boolean {
        val binding = node.resolveBinding()
        if (binding == null) {
            log.warn("Fully qualified name resolution of class '${node.name.fullyQualifiedName}' is not possible. Class is skipped and not parsed")
            return true
        } else {
            fullyQualifiedClassName = binding.qualifiedName
        }

        node.methods.forEach {
            visit(it)
        }

        node.fields.forEach {
            visit(it)
        }

        node.modifiers().forEach {
            if (it is Annotation) {
                visit(it)
            }
        }

        val bf = fvs.map { it.benchField }
        benchClass.setJmhParams(bf)

        return super.visit(node)
    }

    override fun visit(node: FieldDeclaration): Boolean {
        val fv = JdtBenchFieldVisitor()
        fv.visit(node)
        fvs.add(fv)
        return super.visit(node)
    }

    override fun visit(node: NormalAnnotation): Boolean {
        visitAnnotation(node)
        return super.visit(node)
    }

    override fun visit(node: MarkerAnnotation): Boolean {
        visitAnnotation(node)
        return super.visit(node)
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        visitAnnotation(node)
        return super.visit(node)
    }

    abstract fun visitAnnotation(node: Annotation)
}