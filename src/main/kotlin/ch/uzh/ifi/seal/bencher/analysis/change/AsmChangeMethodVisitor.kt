package ch.uzh.ifi.seal.bencher.analysis.change

import org.objectweb.asm.*


class AsmChangeMethodVisitor(api: Int, mv: MethodVisitor?) : MethodVisitor(api, mv) {

    val sb = StringBuilder()

    // sub visitors
    val avs: MutableList<AsmChangeAnnotationVisitor> = mutableListOf()

    fun string(): String = sb.toString()

//    override fun visitParameter(name: String, access: Int) {
//        mv?.visitParameter(name, access)
//        sb.append(name)
//        sb.append(access)
//    }

//    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
//        sb.append(descriptor)
//        sb.append(visible)
//
//        val av = AsmChangeAnnotationVisitor(api, mv?.visitAnnotation(descriptor, visible))
//        avs.add(av)
//        return av
//    }

//    override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath, descriptor: String, visible: Boolean): AnnotationVisitor? {
//        sb.append(typeRef)
//        addTypePath(typePath)
//        sb.append(descriptor)
//        sb.append(visible)
//
//        val av = AsmChangeAnnotationVisitor(api, mv?.visitTypeAnnotation(typeRef, typePath, descriptor, visible))
//        avs.add(av)
//        return av
//    }

//    override fun visitAnnotableParameterCount(parameterCount: Int, visible: Boolean) {
//        mv?.visitAnnotableParameterCount(parameterCount, visible)
//        sb.append(parameterCount)
//        sb.append(visible)
//    }

//    override fun visitParameterAnnotation(parameter: Int, descriptor: String, visible: Boolean): AnnotationVisitor? {
//        sb.append(parameter)
//        sb.append(descriptor)
//        sb.append(visible)
//
//        val av = AsmChangeAnnotationVisitor(api, mv?.visitParameterAnnotation(parameter, descriptor, visible))
//        avs.add(av)
//        return av
//    }

//    override fun visitAttribute(attribute: Attribute) {
//        mv?.visitAttribute(attribute)
//        sb.append(attribute.type)
//        sb.append(attribute.isCodeAttribute)
//        sb.append(attribute.isUnknown)
//    }

    override fun visitFrame(type: Int, nLocal: Int, local: Array<Any>, nStack: Int, stack: Array<Any>) {
        mv?.visitFrame(type, nLocal, local, nStack, stack)
        sb.append(type)
        sb.append(nLocal)
        local.forEach { sb.append(it) }
        stack.forEach { sb.append(it) }
    }

    override fun visitInsn(opcode: Int) {
        mv?.visitInsn(opcode)
        sb.append(opcode)
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {
        mv?.visitIntInsn(opcode, operand)
        sb.append(opcode)
        sb.append(operand)
    }

    override fun visitVarInsn(opcode: Int, `var`: Int) {
        mv?.visitVarInsn(opcode, `var`)
        sb.append(opcode)
        sb.append(`var`)
    }

    override fun visitTypeInsn(opcode: Int, type: String) {
        mv?.visitTypeInsn(opcode, type)
        sb.append(opcode)
        sb.append(type)
    }

    override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
        mv?.visitFieldInsn(opcode, owner, name, descriptor)
        sb.append(opcode)
        sb.append(owner)
        sb.append(name)
        sb.append(descriptor)
    }

    override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
        mv?.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        sb.append(opcode)
        sb.append(owner)
        sb.append(name)
        sb.append(descriptor)
        sb.append(isInterface)
    }

    override fun visitInvokeDynamicInsn(name: String, descriptor: String, bootstrapMethodHandle: Handle, vararg bootstrapMethodArguments: Any) {
        mv?.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments)
        sb.append(name)
        sb.append(descriptor)
        sb.append(bootstrapMethodHandle.desc)
        sb.append(bootstrapMethodHandle.isInterface)
        sb.append(bootstrapMethodHandle.name)
        sb.append(bootstrapMethodHandle.owner)
        sb.append(bootstrapMethodHandle.tag)
        bootstrapMethodArguments.forEach { sb.append(it) }
    }

    override fun visitJumpInsn(opcode: Int, label: Label) {
        mv?.visitJumpInsn(opcode, label)
        sb.append(opcode)
        addLabel(label)
    }

    override fun visitLabel(label: Label) {
        mv?.visitLabel(label)
        addLabel(label)
    }

    override fun visitLdcInsn(value: Any) {
        mv?.visitLdcInsn(value)
        sb.append(value)
    }

    override fun visitIincInsn(`var`: Int, increment: Int) {
        mv?.visitIntInsn(`var`, increment)
        sb.append(`var`)
        sb.append(increment)
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label, vararg labels: Label) {
        mv?.visitTableSwitchInsn(min, max, dflt, *labels)
        sb.append(min)
        sb.append(max)
        addLabel(dflt)
        addLabels(labels)
    }

    override fun visitLookupSwitchInsn(dflt: Label, keys: IntArray, labels: Array<Label>) {
        mv?.visitLookupSwitchInsn(dflt, keys, labels)
        addLabel(dflt)
        keys.forEach { sb.append(it) }
        addLabels(labels)
    }

    override fun visitMultiANewArrayInsn(descriptor: String, numDimensions: Int) {
        mv?.visitMultiANewArrayInsn(descriptor, numDimensions)
        sb.append(descriptor)
        sb.append(numDimensions)
    }

    override fun visitInsnAnnotation(typeRef: Int, typePath: TypePath, descriptor: String, visible: Boolean): AnnotationVisitor? {
        sb.append(typeRef)
        addTypePath(typePath)
        sb.append(descriptor)
        sb.append(visible)

        val av = AsmChangeAnnotationVisitor(api, mv?.visitInsnAnnotation(typeRef, typePath, descriptor, visible))
        avs.add(av)
        return av
    }

    override fun visitTryCatchBlock(start: Label, end: Label, handler: Label, type: String?) {
        mv?.visitTryCatchBlock(start, end, handler, type)
        addLabel(start)
        addLabel(end)
        addLabel(handler)
        if (type != null) {
            sb.append(type)
        }
    }

    override fun visitTryCatchAnnotation(typeRef: Int, typePath: TypePath, descriptor: String, visible: Boolean): AnnotationVisitor? {
        sb.append(typeRef)
        addTypePath(typePath)
        sb.append(descriptor)
        sb.append(visible)

        val av = AsmChangeAnnotationVisitor(api, mv?.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible))
        avs.add(av)
        return av
    }

    override fun visitLocalVariable(name: String, descriptor: String, signature: String?, start: Label, end: Label, index: Int) {
        mv?.visitLocalVariable(name, descriptor, signature, start, end, index)
        sb.append(name)
        sb.append(descriptor)
        if (signature != null) {
            sb.append(signature)
        }
        addLabel(start)
        addLabel(end)
        sb.append(index)
    }

    override fun visitLocalVariableAnnotation(
            typeRef: Int,
            typePath: TypePath,
            start: Array<Label>,
            end: Array<Label>,
            index: IntArray,
            descriptor: String,
            visible: Boolean
    ): AnnotationVisitor? {

        sb.append(typeRef)
        addTypePath(typePath)
        addLabels(start)
        addLabels(end)
        index.forEach { sb.append(it) }
        sb.append(descriptor)
        sb.append(visible)

        val av = AsmChangeAnnotationVisitor(api, mv?.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible))
        avs.add(av)
        return av
    }

//    override fun visitLineNumber(line: Int, start: Label) {
//        mv?.visitLineNumber(line, start)
//        sb.append(line)
//        addLabel(start)
//    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        mv?.visitMaxs(maxStack, maxLocals)
        sb.append(maxStack)
        sb.append(maxLocals)
    }

    override fun visitEnd() {
        mv?.visitEnd()
        avs.forEach { sb.append(it.string()) }
    }

    // helper functions
    private fun addLabel(label: Label) {
        sb.append(label.info)
        //sb.append(label.offset)
    }

    private fun addLabels(labels: Array<out Label>) = labels.forEach { addLabel(it) }

    private fun addTypePath(typePath: TypePath) {
        sb.append(typePath.length)
        for (i in 0 until typePath.length) {
            sb.append(typePath.getStep(i))
            sb.append(typePath.getStepArgument(i))
        }
    }
}
