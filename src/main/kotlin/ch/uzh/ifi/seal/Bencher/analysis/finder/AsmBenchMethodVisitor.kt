package ch.uzh.ifi.seal.bencher.analysis.finder

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor

class AsmBenchMethodVisitor(api: Int, mv: MethodVisitor?, val name: String, val descriptor: String) : MethodVisitor(api, mv) {

    private var isBench: Boolean = false

    fun isBench(): Boolean = isBench

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        if (descriptor == jmhAnnotationBenchmark) {
            isBench = true
        }

        return mv?.visitAnnotation(descriptor, visible)
    }

    companion object {
        private val jmhAnnotationBenchmark = "Lorg/openjdk/jmh/annotations/Benchmark;"
    }
}
