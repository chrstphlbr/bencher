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
        val res = super.visit(node)

        val bm = mvs.map { it.benchMethod }
        benchClass.setBenchs(fullyQualifiedClassName, bm)

        benchClass.setClassExecInfo()

        return res
    }

    override fun visit(node: MethodDeclaration): Boolean {
        val mv = JdtBenchmarkMethodVisitor(fullyQualifiedClassName)
        mv.visit(node)
        mvs.add(mv)
        return super.visit(node)
    }

    override fun visitAnnotation(node: Annotation) {
        when (FullyQualifiedNameHelper.get(node)) {
            JMHConstants.Annotation.fork -> {
                val av = JdtBenchForkAnnotationVisitor()
                benchClass.forkVisitor = av.benchForkAnnotation
                av.visit(node)
            }
            JMHConstants.Annotation.measurement -> {
                val av = JdtBenchIterationAnnotationVisitor()
                benchClass.measurementVisitor = av.benchIterationAnnotation
                av.visit(node)
            }
            JMHConstants.Annotation.warmup -> {
                val av = JdtBenchIterationAnnotationVisitor()
                benchClass.warmupVisitor = av.benchIterationAnnotation
                av.visit(node)
            }
            JMHConstants.Annotation.mode -> {
                val av = JdtBenchModeAnnotationVisitor()
                benchClass.benchModeVisitor = av.benchModeAnnotation
                av.visit(node)
            }
            JMHConstants.Annotation.outputTimeUnit -> {
                val av = JdtBenchOutputTimeUnitAnnotationVisitor()
                benchClass.outputTimeUnitAnnotationVisitor = av.benchOutputTimeUnitAnnotation
                av.visit(node)
            }
        }
    }
}