package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import ch.uzh.ifi.seal.bencher.analysis.JMHConstants
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchClass
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.StateObjectManager
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor

class AsmBenchClassVisitor(api: Int, cv: ClassVisitor?, private val className: String, som: StateObjectManager? = null) : ClassVisitor(api, cv) {
    val benchClass = BenchClass(som)

    // sub visitor
    private val mvs: MutableList<AsmBenchMethodVisitor> = mutableListOf()
    private val fvs: MutableList<AsmBenchFieldVisitor> = mutableListOf()

    fun benchs(): Set<Benchmark> = benchClass.benchs
    fun setups(): Set<SetupMethod> = benchClass.setups
    fun tearDowns(): Set<TearDownMethod> = benchClass.tearDowns
    // returns Some iff benchs.size > 0
    fun classExecInfo() = benchClass.classExecConfig

    fun benchExecInfos() = benchClass.benchExecInfos

    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<String>?): MethodVisitor? {
        val mv = cv?.visitMethod(access, name, descriptor, signature, exceptions)

        val jmhMv = AsmBenchMethodVisitor(
                api = api,
                mv = mv,
                name = name,
                descriptor = descriptor
        )
        mvs.add(jmhMv)
        return jmhMv
    }

    override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
        val fv = AsmBenchFieldVisitor(
                api = api,
                fv = cv?.visitField(access, name, descriptor, signature, value),
                name = name
        )

        fvs.add(fv)

        return fv
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        val sv = cv?.visitAnnotation(descriptor, visible)
        return when (descriptor) {
            JMHConstants.Annotation.fork -> {
                val fv = AsmBenchForkAnnotationVisitor(api, sv)
                benchClass.forkVisitor = fv.benchForkAnnotation
                fv
            }
            JMHConstants.Annotation.measurement -> {
                val mv = AsmBenchIterationAnnotationVisitor(api, sv)
                benchClass.measurementVisitor = mv.benchIterationAnnotation
                mv
            }
            JMHConstants.Annotation.warmup -> {
                val wv = AsmBenchIterationAnnotationVisitor(api, sv)
                benchClass.warmupVisitor = wv.benchIterationAnnotation
                wv
            }
            JMHConstants.Annotation.mode -> {
                val bmv = AsmBenchModeAnnotationVisitor(api, sv)
                benchClass.benchModeVisitor = bmv.benchModeAnnotation
                bmv
            }
            JMHConstants.Annotation.outputTimeUnit -> {
                val otuv = AsmBenchOutputTimeUnitAnnotationVisitor(api, sv)
                benchClass.outputTimeUnitAnnotationVisitor = otuv.benchOutputTimeUnitAnnotation
                otuv
            }
            else -> sv
        }
    }

    override fun visitEnd() {
        cv?.visitEnd()

        val bf = fvs.map { it.benchField }
        benchClass.setJmhParams(bf)

        val bm = mvs.map { it.benchMethod }
        benchClass.setBenchs(className, bm)

        benchClass.setClassExecInfo()
    }
}
