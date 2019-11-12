package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchMethod
import ch.uzh.ifi.seal.bencher.sha265
import org.apache.logging.log4j.LogManager
import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.Annotation

class JdtBenchmarkMethodVisitor(private val className: String) : ASTVisitor() {
    private val log = LogManager.getLogger(JdtBenchmarkMethodVisitor::class.java.canonicalName)

    val benchMethod = BenchMethod()

    override fun visit(node: MethodDeclaration): Boolean {
        benchMethod.name = node.name.fullyQualifiedName
        if (node.body != null) {
            val methodBodySourceCode = node.body.toString().substringAfter("{\n").substringBeforeLast("}")
            benchMethod.numberOfLines = methodBodySourceCode.trim().lines().size
            benchMethod.hash = methodBodySourceCode.sha265
        }

        resolveReturnType(node.returnType2)

        node.modifiers().forEach {
            if (it is Annotation) {
                startAnnotationVisitor(it)
            }
        }

        if (benchMethod.isBench || benchMethod.isSetup || benchMethod.isTearDown) {
            val params = mutableListOf<String>()
            node.parameters().forEach {
                if (it is SingleVariableDeclaration) {
                    val binding = it.type.resolveBinding()

                    when {
                        FullyQualifiedNameHelper.checkIfInfrastructureClass(it.type, "Blackhole", JMHConstants.Class.blackhole) -> params.add(JMHConstants.Class.blackhole)
                        FullyQualifiedNameHelper.checkIfInfrastructureClass(it.type, "Control", JMHConstants.Class.control) -> params.add(JMHConstants.Class.control)
                        FullyQualifiedNameHelper.checkIfInfrastructureClass(it.type, "BenchmarkParams", JMHConstants.Class.benchmarkParams) -> params.add(JMHConstants.Class.benchmarkParams)
                        FullyQualifiedNameHelper.checkIfInfrastructureClass(it.type, "IterationParams", JMHConstants.Class.iterationParams) -> params.add(JMHConstants.Class.iterationParams)
                        FullyQualifiedNameHelper.checkIfInfrastructureClass(it.type, "ThreadParams", JMHConstants.Class.threadParams) -> params.add(JMHConstants.Class.threadParams)
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
        return false
    }

    private fun startAnnotationVisitor(node: Annotation) {
        when (FullyQualifiedNameHelper.get(node)) {
            JMHConstants.Annotation.benchmark -> benchMethod.isBench = true
            JMHConstants.Annotation.setup -> benchMethod.isSetup = true
            JMHConstants.Annotation.tearDown -> benchMethod.isTearDown = true
            JMHConstants.Annotation.group -> {
                val av = JdtBenchGroupAnnotationVisitor()
                benchMethod.groupVisitor = av.benchGroupAnnotation
                node.accept(av)
            }
            JMHConstants.Annotation.fork -> {
                val av = JdtBenchForkAnnotationVisitor()
                benchMethod.forkVisitor = av.benchForkAnnotation
                node.accept(av)
            }
            JMHConstants.Annotation.measurement -> {
                val av = JdtBenchIterationAnnotationVisitor()
                benchMethod.measurementVisitor = av.benchIterationAnnotation
                node.accept(av)
            }
            JMHConstants.Annotation.warmup -> {
                val av = JdtBenchIterationAnnotationVisitor()
                benchMethod.warmupVisitor = av.benchIterationAnnotation
                node.accept(av)
            }
            JMHConstants.Annotation.mode -> {
                val av = JdtBenchModeAnnotationVisitor()
                benchMethod.benchModeVisitor = av.benchModeAnnotation
                node.accept(av)
            }
            JMHConstants.Annotation.outputTimeUnit -> {
                val av = JdtBenchOutputTimeUnitAnnotationVisitor()
                benchMethod.outputTimeUnitAnnotationVisitor = av.benchOutputTimeUnitAnnotation
                node.accept(av)
            }
        }
    }

    private fun resolveReturnType(type: Type?) {
        if (type == null) {
            benchMethod.returnType = SourceCodeConstants.void
        } else if (type is PrimitiveType && type.primitiveTypeCode == PrimitiveType.VOID) {
            benchMethod.returnType = SourceCodeConstants.void
        } else {
            val binding = type.resolveBinding()
            if (binding != null) {
                benchMethod.returnType = binding.qualifiedName
            } else {
                benchMethod.returnType = SourceCodeConstants.void
            }
        }
    }
}