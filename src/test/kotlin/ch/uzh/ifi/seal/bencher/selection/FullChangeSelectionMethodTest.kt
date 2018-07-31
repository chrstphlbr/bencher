package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.change.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FullChangeSelectionMethodTest : AbstractFullChangeSelectionTest() {

    @Test
    fun changeReachableMethod() {
        val a = cs.affected(b1.bench1, MethodChange(JarTestHelper.CoreA.m), fullCg)
        Assertions.assertTrue(a)
    }

    @Test
    fun changeNonReachableMethod() {
        val a = cs.affected(b1.bench1, MethodChange(JarTestHelper.CoreD.m), fullCg)
        Assertions.assertFalse(a)
    }

    @Test
    fun changeConstructorReachableMethod() {
        val a = cs.affected(b1.bench1, MethodChange(JarTestHelper.CoreA.constructor), fullCg)
        Assertions.assertTrue(a)
        val a1 = cs.affected(b1.bench1, MethodChange(PlainMethod(
                clazz = JarTestHelper.CoreA.fqn,
                name = "<clinit>",
                params = listOf()
        )), fullCg)
        Assertions.assertTrue(a1)
    }

    @Test
    fun changeConstructorNonReachableMethod() {
        val a = cs.affected(b1.bench1, MethodChange(PlainMethod(
                clazz = JarTestHelper.CoreD.fqn,
                name = "<init>",
                params = listOf()
        )), fullCg)
        Assertions.assertFalse(a)

        val a1 = cs.affected(b1.bench1, MethodChange(PlainMethod(
                clazz = JarTestHelper.CoreD.fqn,
                name = "<clinit>",
                params = listOf()
        )), fullCg)
        Assertions.assertFalse(a1)
    }

    private fun changeAffected(c: Change, affected: Boolean) {
        // change
        val a1 = cs.affected(b1.bench1, c, fullCg)
        if (affected) {
            Assertions.assertTrue(a1)
        } else {
            Assertions.assertFalse(a1)
        }

        // addition
        val a2 = cs.affected(b1.bench1, AdditionChange(c), fullCg)
        if (affected) {
            Assertions.assertTrue(a2)
        } else {
            Assertions.assertFalse(a2)
        }

        // deletion
        val a3 = cs.affected(b1.bench1, DeletionChange(c), fullCg)
        if (affected) {
            Assertions.assertTrue(a3)
        } else {
            Assertions.assertFalse(a3)
        }
    }

    @Test
    fun changeFieldReachableMethod() {
        val cf = ClassFieldChange(
                clazz = Class(file = "", name = JarTestHelper.CoreA.fqn),
                field = "someField"
        )
        changeAffected(cf, true)
    }

    @Test
    fun changeFieldNonReachableMethod() {
        val cf = ClassFieldChange(
                clazz = Class(file = "", name = JarTestHelper.CoreD.fqn),
                field = "someField"
        )
        changeAffected(cf, false)
    }

    @Test
    fun changeMethodReachableMethod() {
        val m = JarTestHelper.CoreA
        val cm = ClassMethodChange(
                clazz = Class(file = "", name = m.fqn),
                method = PlainMethod(clazz = m.fqn, name = "someMethod", params = listOf())
        )
        changeAffected(cm, false)
    }

    @Test
    fun changeMethodNonReachableMethod() {
        val m = JarTestHelper.CoreD
        val cm = ClassMethodChange(
                clazz = Class(file = "", name = m.fqn),
                method = PlainMethod(clazz = m.fqn, name = "someMethod", params = listOf())
        )
        changeAffected(cm, false)
    }

    @Test
    fun changeClassReachableMethod() {
        val m = JarTestHelper.CoreA
        val ch = ClassHeaderChange(clazz = Class(file = "", name = m.fqn))
        changeAffected(ch, true)
    }
}
