package ch.uzh.ifi.seal.bencher.analysis.coverage.sta

import ch.uzh.ifi.seal.bencher.MF
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.ByteCodeConstants
import ch.uzh.ifi.seal.bencher.analysis.JMHConstants
import ch.uzh.ifi.seal.bencher.analysis.sourceCode
import com.ibm.wala.classLoader.IMethod

fun IMethod.isJMHSetup(): Boolean =
        this.annotations.any { a ->
            val n = a.type.name.toUnicodeString()
            n.contains(JMHConstants.Annotation.setup.substringBeforeLast(';'))
        }

fun IMethod.isJMHTearDown(): Boolean =
        this.annotations.any { a ->
            val n = a.type.name.toUnicodeString()
            n.contains(JMHConstants.Annotation.tearDown.substringBeforeLast(';'))
        }

fun IMethod.bencherMethod(): Method {
    val params = if (this.descriptor.parameters == null) {
        listOf()
    } else {
        this.descriptor.parameters.map { it.toUnicodeString().sourceCode }
    }

    val clazz = this.reference.declaringClass.name.toUnicodeString().sourceCode
    val name = if (this.isInit) {
        ByteCodeConstants.constructor
    } else if (this.isClinit) {
        ByteCodeConstants.staticInit
    } else {
        this.name.toUnicodeString()
    }

    return MF.plainMethod(
            clazz = clazz,
            name = name,
            params = params
    )
}
