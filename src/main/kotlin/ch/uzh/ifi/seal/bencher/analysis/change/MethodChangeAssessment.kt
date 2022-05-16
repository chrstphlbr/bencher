package ch.uzh.ifi.seal.bencher.analysis.change

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import ch.uzh.ifi.seal.bencher.analysis.ByteCodeConstants

interface MethodChangeAssessment {
    fun methodChanged(m: Method, c: Change): Boolean

    fun methodChanged(m: Method, cs: Iterable<Change>): Boolean =
        cs.any { methodChanged(m, it) }
}

object FullMethodChangeAssessment : MethodChangeAssessment {
    // extensive tests in FullChangeSelectorBenchmarkTest and FullChangeSelectorMethodTest

    // returns true iff
    //  (1) method has changed,
    //  (2) the method's class constructor or static initializer has changed
    //  (3) the method's setup methods have changed (if available)
    //  (4) the method's teardown methods have changed (if available)
    //  (5) a field in the containing class has changed, or
    //  (6) the class header (including annotations, e.g., how the benchmark is run)
    //  (7) an affected field, initializer, class was removed or introduced
    override fun methodChanged(m: Method, c: Change): Boolean =
        when (c) {
            // (1), (2), (3), (4)
            is MethodChange -> isMethodConstructorSetupOrTearDown(m, c.method)
            // (5)
            is ClassFieldChange -> c.clazz.name == m.clazz
            // (6)
            is ClassHeaderChange -> c.clazz.name == m.clazz
            // (1), (2), (3), (4)
            is ClassMethodChange -> isMethodConstructorSetupOrTearDown(m, c.method)
            // (7)
            is DeletionChange -> methodChanged(m, c.type)
            // (7)
            is AdditionChange -> methodChanged(m, c.type)

            // not enough information available in Method to check if changed line is part of the method
            is LineChange -> false
        }

    private fun isMethodConstructorSetupOrTearDown(m: Method, changedMethod: Method): Boolean =
        if (m == changedMethod) {
            // (1)
            true
        } else if (m.clazz == changedMethod.clazz) {
            if (changedMethod.name == ByteCodeConstants.constructor || changedMethod.name == ByteCodeConstants.staticInit) {
                // (2)
                true
            } else if (changedMethod is SetupMethod) {
                // (3)
                true
            } else if (changedMethod is TearDownMethod) {
                // (4)
                true
            } else {
                false
            }
        } else {
            false
        }
}
