package ch.uzh.ifi.seal.bencher.analysis.coverage.sta

import ch.uzh.ifi.seal.bencher.Method
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.classLoader.IClassLoader
import com.ibm.wala.classLoader.IField
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.core.util.strings.Atom
import com.ibm.wala.ipa.cha.IClassHierarchy
import com.ibm.wala.types.Selector
import com.ibm.wala.types.TypeName
import com.ibm.wala.types.TypeReference
import com.ibm.wala.types.annotations.Annotation
import java.io.Reader

class IClassMock(private val m: Method) : IClass {
    override fun getClassHierarchy(): IClassHierarchy? = null

    override fun getClassInitializer(): IMethod? = null

    override fun isPublic(): Boolean = true

    override fun getReference(): TypeReference? = null

    override fun getSource(): Reader? = null

    override fun getName(): TypeName = TypeName.findOrCreate(m.clazz)

    override fun getClassLoader(): IClassLoader? = null

    override fun getMethod(selector: Selector?): IMethod = IMethodMock(m)

    override fun getSuperclass(): IClass? = null

    override fun getAllInstanceFields(): MutableCollection<IField> = mutableListOf()

    override fun getAllMethods(): MutableCollection<IMethod> = mutableListOf(IMethodMock(m))

    override fun isInterface(): Boolean = false

    override fun getAllImplementedInterfaces(): MutableCollection<IClass> = mutableListOf()

    override fun isArrayClass(): Boolean = false

    override fun isReferenceType(): Boolean = false

    override fun getAllStaticFields(): MutableCollection<IField> = mutableListOf()

    override fun getField(name: Atom?): IField? = null

    override fun getField(name: Atom?, type: TypeName?): IField? = null

    override fun getModifiers(): Int = 0

    override fun getSourceFileName(): String = ""

    override fun getDeclaredStaticFields(): MutableCollection<IField> = mutableListOf()

    override fun isAbstract(): Boolean = false

    override fun getDeclaredMethods(): MutableCollection<IMethod> = allMethods

    override fun getAnnotations(): MutableCollection<Annotation> = mutableListOf()

    override fun getAllFields(): MutableCollection<IField> = mutableListOf()

    override fun getDeclaredInstanceFields(): MutableCollection<IField> = mutableListOf()

    override fun getDirectInterfaces(): MutableCollection<out IClass> = mutableListOf()

    override fun isPrivate(): Boolean = false

    override fun isSynthetic(): Boolean = false
}
