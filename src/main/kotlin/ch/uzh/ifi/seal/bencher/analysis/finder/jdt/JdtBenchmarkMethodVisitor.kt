package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.asm.JdtBenchOutputTimeUnitAnnotationVisitor
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchMethod
import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration
import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.Annotation
import org.funktionale.option.Option

class JdtBenchmarkMethodVisitor : ASTVisitorExtended() {
    lateinit var name: String

    private val benchMethod = BenchMethod()

    fun isBench(): Boolean = benchMethod.isBench
    fun isSetup(): Boolean = benchMethod.isSetup
    fun isTearDown(): Boolean = benchMethod.isTearDown
    // is Some iff isBench == true
    fun execInfo(): Option<ExecutionConfiguration> = benchMethod.execConfig

    override fun visit(node: MethodDeclaration): Boolean {
        name = node.name.fullyQualifiedName

        val x = node.modifiers()
        node.modifiers().forEach {
            if (it is Annotation) {
                visit(it)
            }
        }

        benchMethod.setExecInfo()
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

    private fun visitAnnotation(node: Annotation) {
        val name = node.resolveTypeBinding().qualifiedName
        when (name) {
            JMHConstants.Annotation.benchmark -> benchMethod.isBench = true
            JMHConstants.Annotation.setup -> benchMethod.isSetup = true
            JMHConstants.Annotation.tearDown -> benchMethod.isTearDown = true
            JMHConstants.Annotation.fork -> {
                val av = JdtBenchForkAnnotationVisitor()
                benchMethod.forkVisitor = av.benchForkAnnotation
                av.visit(node)
            }
            JMHConstants.Annotation.measurement -> {
                val av = JdtBenchIterationAnnotationVisitor()
                benchMethod.measurementVisitor = av.benchIterationAnnotation
                av.visit(node)
            }
            JMHConstants.Annotation.warmup -> {
                val av = JdtBenchIterationAnnotationVisitor()
                benchMethod.warmupVisitor = av.benchIterationAnnotation
                av.visit(node)
            }
            JMHConstants.Annotation.mode -> {
                val av = JdtBenchModeAnnotationVisitor()
                benchMethod.benchModeVisitor = av.benchModeAnnotation
                av.visit(node)
            }
            JMHConstants.Annotation.outputTimeUnit -> {
                val av = JdtBenchOutputTimeUnitAnnotationVisitor()
                benchMethod.outputTimeUnitAnnotationVisitor = av.benchOutputTimeUnitAnnotation
                av.visit(node)
            }
        }
    }
}