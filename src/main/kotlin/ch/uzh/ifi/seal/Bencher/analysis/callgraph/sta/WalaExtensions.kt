package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.JMHConstants
import ch.uzh.ifi.seal.bencher.analysis.sourceCode
import com.ibm.wala.classLoader.IMethod

fun IMethod.isJMHSetupTearDown(): Boolean =
        this.annotations.any { a ->
            val n = a.type.name.toUnicodeString()
            n.contains(JMHConstants.annotationSetup) || n.contains(JMHConstants.annotationTearDown)
        }

fun IMethod.bencherMethod(): Method {
    val params = if (this.descriptor.parameters == null) {
        listOf()
    } else {
        this.descriptor.parameters.map { it.toUnicodeString().sourceCode }
    }

    val clazz = this.reference.declaringClass.name.toUnicodeString().sourceCode
    val name = this.name.toUnicodeString()

    return PlainMethod(
            clazz = clazz,
            name = name,
            params = params
    )
}
