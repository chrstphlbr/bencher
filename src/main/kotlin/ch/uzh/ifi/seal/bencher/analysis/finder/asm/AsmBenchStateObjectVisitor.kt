package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.analysis.JMHConstants
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.StateObjectManager
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor

class AsmBenchStateObjectVisitor(api: Int, cv: ClassVisitor?, private val className: String, private val som: StateObjectManager) : ClassVisitor(api, cv) {
    private var isStateObject = false

    // sub visitor
    private val fvs: MutableList<AsmBenchFieldVisitor> = mutableListOf()

    override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
        val fv = AsmBenchFieldVisitor(
                api = api,
                fv = cv?.visitField(access, name, descriptor, signature, value),
                name = name
        )

        fvs.add(fv)

        return fv
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        when (descriptor) {
            JMHConstants.Annotation.state -> {
                isStateObject = true
            }
        }

        return cv?.visitAnnotation(descriptor, visible)
    }

    override fun visitEnd() {
        cv?.visitEnd()

        val bf = fvs.map { it.benchField }

        if (isStateObject) {
            som.add(className, bf)
        }
    }
}
