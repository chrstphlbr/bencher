package ch.uzh.ifi.seal.bencher.analysis.change

import org.objectweb.asm.AnnotationVisitor

class AsmChangeAnnotationVisitor(api: Int, av: AnnotationVisitor?) : AnnotationVisitor(api, av) {
    private val sb = StringBuilder()

    // sub visitors
    private val avs: MutableList<AsmChangeAnnotationVisitor> = mutableListOf()

    fun string(): String = sb.toString()

    override fun visit(name: String, value: Any?) {
        av?.visit(name, value)
        sb.append(name)
        if (value != null) {
            sb.append(value)
        }
    }

    override fun visitEnum(name: String, descriptor: String, value: String) {
        av?.visitEnum(name, descriptor, value)
        sb.append(name)
        sb.append(descriptor)
        sb.append(value)
    }

    override fun visitAnnotation(name: String, descriptor: String): AnnotationVisitor? {
        sb.append(name)
        sb.append(descriptor)
        val av = AsmChangeAnnotationVisitor(api, av?.visitAnnotation(name, descriptor))
        avs.add(av)
        return av
    }

    override fun visitEnd() {
        av?.visitEnd()

        avs.forEach { av ->
            sb.append(av.string())
        }
    }
}
