package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.analysis.JMHConstants
import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration
import org.funktionale.option.Option
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor

class AsmBenchMethodVisitor(api: Int, mv: MethodVisitor?, val name: String, val descriptor: String) : MethodVisitor(api, mv) {

    private var isBench: Boolean = false
    private var isSetup: Boolean = false
    private var isTearDown: Boolean = false
    private lateinit var execConfig: Option<ExecutionConfiguration>

    // sub-visitor
    private var forkVisitor: AsmBenchForkAnnotationVisitor? = null
    private var measurementVisitor: AsmBenchIterationAnnotationVisitor? = null
    private var warmupVisitor: AsmBenchIterationAnnotationVisitor? = null

    fun isBench(): Boolean = isBench
    fun isSetup(): Boolean = isSetup
    fun isTearDown(): Boolean = isTearDown
    // is Some iff isBench == true
    fun execInfo(): Option<ExecutionConfiguration> = execConfig

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        val v = mv?.visitAnnotation(descriptor, visible)

        val av = when (descriptor) {
            JMHConstants.Annotation.benchmark -> {
                isBench = true
                v
            }
            JMHConstants.Annotation.setup -> {
                isSetup = true
                v
            }
            JMHConstants.Annotation.tearDown -> {
                isTearDown = true
                v
            }
            JMHConstants.Annotation.fork -> {
                val fv = AsmBenchForkAnnotationVisitor(api, v)
                forkVisitor = fv
                fv
            }
            JMHConstants.Annotation.measurement -> {
                val mv = AsmBenchIterationAnnotationVisitor(api, v)
                measurementVisitor = mv
                mv
            }
            JMHConstants.Annotation.warmup -> {
                val wv = AsmBenchIterationAnnotationVisitor(api, v)
                warmupVisitor = wv
                wv
            }
            else -> v
        }

        return av
    }

    override fun visitEnd() {
        mv?.visitEnd()
        setExecInfo()
    }

    private fun setExecInfo() {
        val (f, wf) = if (forkVisitor != null) {
            Pair(forkVisitor!!.forks(), forkVisitor!!.warmups())
        } else {
            Pair(-1, -1)
        }

        val (wi, wt, wtu) = if (warmupVisitor != null) {
            Triple(warmupVisitor!!.iterations(), warmupVisitor!!.time(), warmupVisitor!!.timeUnit())
        } else {
            Triple(-1, -1, Option.empty())
        }

        val (mi, mt, mtu) = if (measurementVisitor != null) {
            Triple(measurementVisitor!!.iterations(), measurementVisitor!!.time(), measurementVisitor!!.timeUnit())
        } else {
            Triple(-1, -1, Option.empty())
        }

        execConfig = if (isBench()) {
            Option.Some(ExecutionConfiguration(
                    forks = f,
                    warmupForks = wf,
                    measurementIterations = mi,
                    measurementTime = mt,
                    measurementTimeUnit = mtu,
                    warmupIterations = wi,
                    warmupTime = wt,
                    warmupTimeUnit = wtu
            ))
        } else {
            Option.empty()
        }
    }
}
