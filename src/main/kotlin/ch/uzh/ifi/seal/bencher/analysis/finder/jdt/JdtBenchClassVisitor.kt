package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchClass
import org.apache.logging.log4j.LogManager
import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.Annotation

class JdtBenchClassVisitor : ASTVisitorExtended() {
    val log = LogManager.getLogger(JdtBenchClassVisitor::class.java.canonicalName)

    private lateinit var fullyQualifiedClassName: String
    private val benchClass = BenchClass()

    // sub visitor
    private val mvs: MutableList<JdtBenchmarkMethodVisitor> = mutableListOf()
    private val fvs: MutableList<JdtBenchFieldVisitor> = mutableListOf()

    fun benchs(): Set<Benchmark> = benchClass.benchs
    fun setups(): Set<SetupMethod> = benchClass.setups
    fun tearDowns(): Set<TearDownMethod> = benchClass.tearDowns
    // returns Some iff benchs.size > 0
    fun classExecInfo() = benchClass.classExecConfig

    fun benchExecInfos() = benchClass.benchExecInfos
    fun benchClass() =
            if (::fullyQualifiedClassName.isInitialized)
                Pair(fullyQualifiedClassName, benchClass)
            else
                null

    override fun visit(node: TypeDeclaration): Boolean {
        val binding = node.resolveBinding()
        // TODO inner classes correct fully qualified name (no $ in name)
        if (binding == null) {
            log.warn("Fully qualified name resolution of class ${node.name.fullyQualifiedName} is not possible. The class is skipped")
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

        val bm = mvs.map { it.benchMethod }
        benchClass.setBenchs(fullyQualifiedClassName, bm)

        benchClass.setClassExecInfo()

        return super.visit(node)
    }

    override fun visit(node: MethodDeclaration): Boolean {
        val mv = JdtBenchmarkMethodVisitor()
        mv.visit(node)
        mvs.add(mv)
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

    private fun visitAnnotation(node: Annotation) {
        val name = node.resolveTypeBinding().qualifiedName
        when (name) {
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