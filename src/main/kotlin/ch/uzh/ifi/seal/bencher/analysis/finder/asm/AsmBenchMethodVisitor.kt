package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.analysis.JMHConstants
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchMethod
import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration
import org.funktionale.option.Option
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor

class AsmBenchMethodVisitor(api: Int, mv: MethodVisitor?, val name: String, val descriptor: String) : MethodVisitor(api, mv) {
    private val benchMethod = BenchMethod()

    fun isBench(): Boolean = benchMethod.isBench
    fun isSetup(): Boolean = benchMethod.isSetup
    fun isTearDown(): Boolean = benchMethod.isTearDown
    // is Some iff isBench == true
    fun execInfo(): Option<ExecutionConfiguration> = benchMethod.execConfig

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        val v = mv?.visitAnnotation(descriptor, visible)

        val av = when (descriptor) {
            JMHConstants.Annotation.benchmark -> {
                benchMethod.isBench = true
                v
            }
            JMHConstants.Annotation.setup -> {
                benchMethod.isSetup = true
                v
            }
            JMHConstants.Annotation.tearDown -> {
                benchMethod.isTearDown = true
                v
            }
            JMHConstants.Annotation.fork -> {
                val fv = AsmBenchForkAnnotationVisitor(api, v)
                benchMethod.forkVisitor = fv.benchForkAnnotation
                fv
            }
            JMHConstants.Annotation.measurement -> {
                val mv = AsmBenchIterationAnnotationVisitor(api, v)
                benchMethod.measurementVisitor = mv.benchIterationAnnotation
                mv
            }
            JMHConstants.Annotation.warmup -> {
                val wv = AsmBenchIterationAnnotationVisitor(api, v)
                benchMethod.warmupVisitor = wv.benchIterationAnnotation
                wv
            }
            JMHConstants.Annotation.mode -> {
                val bmv = AsmBenchModeAnnotationVisitor(api, v)
                benchMethod.benchModeVisitor = bmv.benchModeAnnotation
                bmv
            }
            JMHConstants.Annotation.outputTimeUnit -> {
                val otuv = AsmBenchOutputTimeUnitAnnotationVisitor(api, v)
                benchMethod.outputTimeUnitAnnotationVisitor = otuv.benchOutputTimeUnitAnnotation
                otuv
            }
            else -> v
        }

        return av
    }

    override fun visitEnd() {
        mv?.visitEnd()
        benchMethod.setExecInfo()
    }
}
