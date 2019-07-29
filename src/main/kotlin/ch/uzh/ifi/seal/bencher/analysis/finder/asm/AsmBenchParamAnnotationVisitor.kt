package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import org.objectweb.asm.AnnotationVisitor

class AsmBenchParamAnnotationVisitor(api: Int, av: AnnotationVisitor?, val fieldName: String) : AnnotationVisitor(api, av) {

    val arrayValues = mutableListOf<String>()

    fun values(): List<String> = arrayValues

    override fun visitArray(name: String): AnnotationVisitor? {
        av?.visitArray(name)
        return this
    }

    override fun visit(name: String?, value: Any) {
        av?.visit(name, value)
        when (value) {
            is String -> arrayValues.add(value)
            else -> return
        }
    }
}
