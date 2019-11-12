package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchField
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.FieldVisitor

class AsmBenchFieldVisitor(api: Int, fv: FieldVisitor?, private val name: String) : FieldVisitor(api, fv) {
    val benchField = BenchField()

    // sub-visitors
    private val avs: MutableList<AsmBenchParamAnnotationVisitor> = mutableListOf()

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? =
            if (descriptor == jmhAnnotationParam) {
                benchField.isParam = true
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
        benchField.jmhParams = avs.associate { pav ->
            Pair(pav.fieldName, pav.arrayValues)
        }.toMutableMap()
    }

    companion object {
        private const val jmhAnnotationParam = "Lorg/openjdk/jmh/annotations/Param;"
    }
}
