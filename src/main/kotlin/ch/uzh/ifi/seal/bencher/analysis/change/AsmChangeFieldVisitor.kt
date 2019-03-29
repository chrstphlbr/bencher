package ch.uzh.ifi.seal.bencher.analysis.change

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Attribute
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.TypePath

class AsmChangeFieldVisitor(private val opcode: Int, fv: FieldVisitor?) : FieldVisitor(opcode, fv) {
    private val sb = StringBuilder()

    // sub visitors
    private val avs: MutableList<AsmChangeAnnotationVisitor> = mutableListOf()

    fun string(): String = sb.toString()

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        sb.append(descriptor)
        sb.append(visible)
        val av = AsmChangeAnnotationVisitor(opcode, fv?.visitAnnotation(descriptor, visible))
        avs.add(av)
        return av
    }

    override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath, descriptor: String, visible: Boolean): AnnotationVisitor? {
        sb.append(typeRef)
        sb.append(typePath)
        sb.append(descriptor)
        sb.append(visible)
        val av = AsmChangeAnnotationVisitor(opcode, fv?.visitTypeAnnotation(typeRef, typePath, descriptor, visible))
        avs.add(av)
        return null
    }

    override fun visitAttribute(attribute: Attribute) {
        fv?.visitAttribute(attribute)
        sb.append(attribute.type)
        sb.append(attribute.isCodeAttribute)
        sb.append(attribute.isUnknown)
    }

    override fun visitEnd() {
        fv?.visitEnd()
        avs.forEach { av ->
            sb.append(av.string())
        }
    }
}
