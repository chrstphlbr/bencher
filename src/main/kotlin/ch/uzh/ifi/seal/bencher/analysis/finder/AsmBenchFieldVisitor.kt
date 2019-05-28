package ch.uzh.ifi.seal.bencher.analysis.finder

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.FieldVisitor


class AsmBenchFieldVisitor(api: Int, fv: FieldVisitor?, private val name: String) : FieldVisitor(api, fv) {
    private var isParam: Boolean = false
    private lateinit var jmhParams: Map<String, List<String>>

    // sub-visitors
    private val avs: MutableList<AsmBenchParamAnnotationVisitor> = mutableListOf()

    fun isParam(): Boolean = isParam
    fun params(): Map<String, List<String>> = jmhParams

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? =
            if (descriptor == jmhAnnotationParam) {
                isParam = true
                val av = AsmBenchParamAnnotationVisitor(
                        api = api,
                        av = fv?.visitAnnotation(descriptor, visible),
                        fieldName = name
                )
                avs.add(av)
                av
            } else {
                fv?.visitAnnotation(descriptor, visible)
            }

    override fun visitEnd() {
        fv?.visitEnd()
        jmhParams = avs.associate { pav ->
            Pair(pav.fieldName, pav.arrayValues)
        }
    }

    companion object {
        private val jmhAnnotationParam = "Lorg/openjdk/jmh/annotations/Param;"
    }
}
