package ch.uzh.ifi.seal.bencher.analysis.finder

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
            jmhAnnotationBenchmark -> isBench = true
            jmhAnnotationSetup -> isSetup = true
            jmhAnnotationTearDown -> isTearDown = true
        }

        return mv?.visitAnnotation(descriptor, visible)
    }

    companion object {
        private val jmhAnnotationBenchmark = "Lorg/openjdk/jmh/annotations/Benchmark;"
        private val jmhAnnotationSetup = "Lorg/openjdk/jmh/annotations/Setup;"
        private val jmhAnnotationTearDown = "Lorg/openjdk/jmh/annotations/TearDown;"
    }
}
