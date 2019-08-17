package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchMethod
import org.apache.logging.log4j.LogManager
import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.Annotation

class JdtBenchmarkMethodVisitor(private val className: String) : ASTVisitorExtended() {
    private val log = LogManager.getLogger(JdtBenchmarkMethodVisitor::class.java.canonicalName)

    val benchMethod = BenchMethod()

    override fun visit(node: MethodDeclaration): Boolean {
        benchMethod.name = node.name.fullyQualifiedName

        node.modifiers().forEach {
            if (it is Annotation) {
                visit(it)
            }
        }

        // TODO params nur bestimmen wenn relevant
        if (benchMethod.isBench || benchMethod.isSetup || benchMethod.isTearDown) {
            val params = mutableListOf<String>()
            node.parameters().forEach {
                if (it is SingleVariableDeclaration) {
                    val binding = it.type.resolveBinding()

                    when {
                        FullyQualifiedNameHelper.checkIfBlackhole(it.type) -> params.add(JMHConstants.Class.blackhole)
                        binding == null -> {
                            log.warn("Fully qualified name resolution of parameter type '${it.type}' in method '${benchMethod.name}' in class '$className' is not possible (Parameters from external dependencies cannot be resolved). Parameter is skipped")
                            params.add(it.type.toString())
                        }
                        else -> params.add(binding.qualifiedName)
                    }
                }
            }

            benchMethod.params = params

            benchMethod.setExecInfo()
        }
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
        when (FullyQualifiedNameHelper.get(node)) {
            JMHConstants.Annotation.benchmark -> benchMethod.isBench = true
            JMHConstants.Annotation.setup -> benchMethod.isSetup = true
            JMHConstants.Annotation.tearDown -> benchMethod.isTearDown = true
            JMHConstants.Annotation.group -> {
                val av = JdtBenchGroupAnnotationVisitor()
                benchMethod.groupVisitor = av.benchGroupAnnotation
                av.visit(node)
            }
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