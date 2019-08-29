package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.StateObjectManager
import org.eclipse.jdt.core.dom.Annotation
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.TypeDeclaration

class JdtBenchClassVisitor(som: StateObjectManager? = null) : JdtBenchAbstractClassVisitor(som) {
    // sub visitor
    private val mvs: MutableList<JdtBenchmarkMethodVisitor> = mutableListOf()

    fun benchs(): Set<Benchmark> = benchClass.benchs

    fun benchClass() =
            if (isFqnInit())
                Pair(fullyQualifiedClassName, benchClass)
            else
                null

    override fun visit(node: TypeDeclaration): Boolean {
        super.visit(node)

        node.methods.forEach {
            visitMethod(it)
        }

        val bm = mvs.map { it.benchMethod }
        benchClass.setBenchs(fullyQualifiedClassName, bm)

        benchClass.setClassExecInfo()

        return false
    }

    fun visitMethod(node: MethodDeclaration) {
        val mv = JdtBenchmarkMethodVisitor(fullyQualifiedClassName)
        node.accept(mv)
        mvs.add(mv)
    }

    override fun visitAnnotation(node: Annotation) {
        when (FullyQualifiedNameHelper.get(node)) {
            JMHConstants.Annotation.fork -> {
                val av = JdtBenchForkAnnotationVisitor()
                benchClass.forkVisitor = av.benchForkAnnotation
                node.accept(av)
            }
            JMHConstants.Annotation.measurement -> {
                val av = JdtBenchIterationAnnotationVisitor()
                benchClass.measurementVisitor = av.benchIterationAnnotation
                node.accept(av)
            }
            JMHConstants.Annotation.warmup -> {
                val av = JdtBenchIterationAnnotationVisitor()
                benchClass.warmupVisitor = av.benchIterationAnnotation
                node.accept(av)
            }
            JMHConstants.Annotation.mode -> {
                val av = JdtBenchModeAnnotationVisitor()
                benchClass.benchModeVisitor = av.benchModeAnnotation
                node.accept(av)
            }
            JMHConstants.Annotation.outputTimeUnit -> {
                val av = JdtBenchOutputTimeUnitAnnotationVisitor()
                benchClass.outputTimeUnitAnnotationVisitor = av.benchOutputTimeUnitAnnotation
                node.accept(av)
            }
        }
    }
}