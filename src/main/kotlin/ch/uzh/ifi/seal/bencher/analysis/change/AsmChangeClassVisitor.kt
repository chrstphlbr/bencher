package ch.uzh.ifi.seal.bencher.analysis.change

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.descriptorToParamList
import ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchClassVisitor
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.*

class AsmChangeClassVisitor(api: Int, private val bcv: AsmBenchClassVisitor, private val filepath: String) : ClassVisitor(api, bcv) {
    private val classes = HashMap<ClassHeaderChange, StringBuilder>()
    private val fields = HashMap<ClassFieldChange, StringBuilder>()
    private val methodSignatures = HashMap<String, StringBuilder>()
    private val methodBodies = HashMap<String, StringBuilder>()

    // sub visitors
    val fvs: MutableMap<ClassFieldChange, AsmChangeFieldVisitor> = mutableMapOf()
    val avs: MutableList<AsmChangeAnnotationVisitor> = mutableListOf()
    val mvs: MutableMap<String, AsmChangeMethodVisitor> = mutableMapOf()

    private lateinit var currentClass: ClassHeaderChange

    fun changes(): Map<Change, ByteArray> {
        val ret = mutableMapOf<Change, ByteArray>()
        ret.putAll(hashes(classes))
        ret.putAll(hashes(fields))
        ret.putAll(methodHashes(methodSignatures, true))
        ret.putAll(methodHashes(methodBodies, false))
        return ret
    }

    private fun <T : Change> hashes(m: Map<T, StringBuilder>): Map<T, ByteArray> =
            m.mapValues { (_, v) -> v.toString().sha265 }

    private fun methodHashes(m: Map<String, StringBuilder>, signature: Boolean): Map<Change, ByteArray> =
            m.mapNotNull { (fqmn, sb) ->
                if (!signature) {
                    // if not the signature, but the method body
                    val mv = mvs[fqmn] ?: return@mapNotNull null
                    sb.append(mv.string())
                }
                val paramStr = "(${fqmn.substringAfter('(').substringBefore(')')})"
                val desc = descriptorToParamList(paramStr).getOrElse {
                    log.warn("Could not get param list from '$paramStr'")
                    return@mapNotNull null
                }
                val fqcmn = fqmn.substringBefore('(')
                val cn = fqcmn.substringBeforeLast(".").replaceFileSeparatorWithDots
                val mn = fqcmn.substringAfterLast(".")
                val benchs = bcv.benchs().benchmarksFor(cn, mn)
                val method: Method = if (benchs.size > 0) {
                    // create Benchmark MethodChange
                    // take first one if multiple returned
                    benchs.elementAt(0)
                } else {
                    // create PlainMethod MethodChange
                    MF.plainMethod(
                            clazz = cn,
                            name = mn,
                            params = desc
                    )
                }
                val mc = if (signature) {
                    ClassMethodChange(method = method, clazz = currentClass.clazz)
                } else {
                    MethodChange(method = method)
                }
                Pair(mc, sb.toString().sha265)
            }.toMap()

    override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String, interfaces: Array<String>) {
        cv?.visit(version, access, name, signature, superName, interfaces)
        currentClass = ClassHeaderChange(
                clazz = Class(
                        name = name.replace(".class", "").replaceFileSeparatorWithDots
                )
        )
        val sb = StringBuilder()
        sb.append(name)
        sb.append(version)
        sb.append(access)
        if (signature != null) {
            sb.append(signature)
        }
        sb.append(superName)
        sb.append(interfaces.fold("") { acc, i -> "$acc, $i" })
        classes[currentClass] = sb
    }

    override fun visitAttribute(attribute: Attribute) {
        cv?.visitAttribute(attribute)
        val sb = classes[currentClass] ?: throw IllegalStateException("Current class ($currentClass) not existing")
        sb.append(attribute.type)
        sb.append(attribute.isCodeAttribute)
        sb.append(attribute.isUnknown)
    }

    override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
        val sb = StringBuilder()
        sb.append(name)
        sb.append(access)
        sb.append(descriptor)
        if (signature != null) {
            sb.append(signature)
        }
        if (value != null) {
            sb.append(value)
        }

        val fv = AsmChangeFieldVisitor(api, cv?.visitField(access, name, descriptor, signature, value))

        val f = ClassFieldChange(
                field = name,
                clazz = currentClass.clazz
        )
        // add field StringBuilder
        fields[f] = sb

        // add field visitor
        fvs[f] = fv

        return fv
    }

    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<String>?): MethodVisitor? {
        val sb = StringBuilder()
        sb.append(name)
        sb.append(access)
        sb.append(descriptor)
        if (signature != null) {
            sb.append(signature)
        }
        if (exceptions != null) {
            sb.append(exceptions.fold("") { acc, i -> "$acc, $i" })
        }

        val method = methodStr(currentClass.clazz, name, descriptor)
        methodSignatures[method] = sb
        methodBodies[method] = StringBuilder()

        val bv = bcv.visitMethod(access, name, descriptor, signature, exceptions)
        val mv = AsmChangeMethodVisitor(api, bv)
        mvs[method] = mv

        return mv
    }

    private fun methodStr(currentClass: Class, methodName: String, descriptor: String): String =
            "${currentClass.name}.$methodName$descriptor"

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        val sb = classes[currentClass] ?: throw IllegalStateException("Current class ($currentClass) not existing")
        sb.append(descriptor)
        sb.append(visible)

        val av = AsmChangeAnnotationVisitor(api, cv?.visitAnnotation(descriptor, visible))
        avs.add(av)
        return av
    }

    override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath, descriptor: String, visible: Boolean): AnnotationVisitor? {
        val sb = classes[currentClass] ?: throw IllegalStateException("Current class ($currentClass) not existing")
        sb.append(typeRef)
        sb.append(typePath)
        sb.append(descriptor)
        sb.append(visible)

        val av = AsmChangeAnnotationVisitor(api, cv?.visitTypeAnnotation(typeRef, typePath, descriptor, visible))
        avs.add(av)
        return av
    }

    override fun visitEnd() {
        cv?.visitEnd()

        fields.forEach { fc, sb ->
            val vs = fvs[fc] ?: return@forEach
            sb.append(vs.string())
        }

        avs.forEach { av ->
            val cc = classes[currentClass] ?: return@forEach
            cc.append(av.string())
        }

        methodBodies.forEach { mc, sb ->
            val mv = mvs[mc] ?: return@forEach
            sb.append(mv.string())
        }
    }

    companion object {
        private val log = LogManager.getLogger(AsmChangeClassVisitor::class.java.canonicalName)
    }
}
