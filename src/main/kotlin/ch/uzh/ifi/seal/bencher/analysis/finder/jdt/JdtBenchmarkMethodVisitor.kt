package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchMethod
import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.Annotation

class JdtBenchmarkMethodVisitor : ASTVisitorExtended() {
    val benchMethod = BenchMethod()

    override fun visit(node: MethodDeclaration): Boolean {
        benchMethod.name = node.name.fullyQualifiedName

        node.modifiers().forEach {
            if (it is Annotation) {
                visit(it)
            }
        }

        val params = mutableListOf<String>()
        node.parameters().forEach {
            if (it is SingleVariableDeclaration) {
                params.add(it.type.resolveBinding().qualifiedName)
            }
        }

        benchMethod.params = params

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