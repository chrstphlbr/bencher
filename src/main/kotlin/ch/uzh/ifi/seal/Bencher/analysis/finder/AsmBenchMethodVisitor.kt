package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.analysis.JMHConstants
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor

class AsmBenchMethodVisitor(api: Int, mv: MethodVisitor?, val name: String, val descriptor: String) : MethodVisitor(api, mv) {

    private var isBench: Boolean = false
    private var isSetup: Boolean = false
    private var isTearDown: Boolean = false

    fun isBench(): Boolean = isBench
    fun isSetup(): Boolean = isSetup
    fun isTearDown(): Boolean = isTearDown

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        when (descriptor) {
            JMHConstants.Annotation.benchmark -> isBench = true
            JMHConstants.Annotation.setup -> isSetup = true
            JMHConstants.Annotation.tearDown -> isTearDown = true
        }

        return mv?.visitAnnotation(descriptor, visible)
    }
}
