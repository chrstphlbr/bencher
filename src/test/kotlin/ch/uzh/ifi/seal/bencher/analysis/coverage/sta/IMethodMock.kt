package ch.uzh.ifi.seal.bencher.analysis.coverage.sta

import ch.uzh.ifi.seal.bencher.Method
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.core.util.strings.Atom
import com.ibm.wala.ipa.cha.IClassHierarchy
import com.ibm.wala.types.Descriptor
import com.ibm.wala.types.MethodReference
import com.ibm.wala.types.Selector
import com.ibm.wala.types.TypeReference
import com.ibm.wala.types.annotations.Annotation

class IMethodMock(private val m: Method) : IMethod {
    override fun getClassHierarchy(): IClassHierarchy? = null

    override fun hasLocalVariableTable(): Boolean = false
    override fun isAnnotation(): Boolean = false

    override fun isEnum(): Boolean = false

    override fun isModule(): Boolean = false

    override fun isPublic(): Boolean = true

    override fun isInit(): Boolean = false

    override fun getName(): Atom = Atom.findOrCreateUnicodeAtom(m.name)

    override fun getReference(): MethodReference? = null

    override fun isNative(): Boolean = false

    override fun isWalaSynthetic(): Boolean = false

    override fun getSignature(): String = "${m.clazz}.${m.name}(${m.params.reduce { acc, s -> "$acc,$s" }})"

    override fun getParameterSourcePosition(paramNum: Int): IMethod.SourcePosition? = null

    override fun getLocalVariableName(bcIndex: Int, localNumber: Int): String = ""

    override fun getNumberOfParameters(): Int = 0

    override fun isStatic(): Boolean = false

    override fun hasExceptionHandler(): Boolean = false

    override fun isClinit(): Boolean = false

    override fun isBridge(): Boolean = false

    override fun getSourcePosition(instructionIndex: Int): IMethod.SourcePosition? = null

    override fun getParameterType(i: Int): TypeReference? = null

    override fun isProtected(): Boolean = false

    override fun isFinal(): Boolean = false

    override fun getDeclaringClass(): IClass = IClassMock(m)

    override fun isAbstract(): Boolean = false

    override fun getAnnotations(): MutableCollection<Annotation> = mutableListOf()

    override fun getDeclaredExceptions(): Array<TypeReference> = arrayOf()

    override fun getReturnType(): TypeReference? = null

    override fun getLineNumber(bcIndex: Int): Int = 0

    override fun getSelector(): Selector? = null

    override fun getDescriptor(): Descriptor? = null

    override fun isSynthetic(): Boolean = false

    override fun isSynchronized(): Boolean = false

    override fun isPrivate(): Boolean = false
}
