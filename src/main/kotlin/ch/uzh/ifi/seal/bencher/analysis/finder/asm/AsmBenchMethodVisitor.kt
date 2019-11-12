package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.analysis.JMHConstants
import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import ch.uzh.ifi.seal.bencher.analysis.descriptorToParamList
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchMethod
import ch.uzh.ifi.seal.bencher.analysis.sourceCode
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type

class AsmBenchMethodVisitor(api: Int, mv: MethodVisitor?, val name: String, val descriptor: String) : MethodVisitor(api, mv) {
    val benchMethod = BenchMethod(name)

    init {
        val oParams = descriptorToParamList(descriptor)
        benchMethod.params = if (!oParams.isEmpty()) {
            oParams.get()
        } else {
            listOf()
        }

        val returnType = Type.getReturnType(descriptor)
        if (returnType == Type.VOID_TYPE) {
            benchMethod.returnType = SourceCodeConstants.void
        } else {
            benchMethod.returnType = returnType.descriptor.sourceCode
        }
    }

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
            JMHConstants.Annotation.group -> {
                val fv = AsmBenchGroupAnnotationVisitor(api, v)
                benchMethod.groupVisitor = fv.benchGroupAnnotation
                fv
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
