package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import ch.uzh.ifi.seal.bencher.analysis.change.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FullChangeSelectorMethodTest : AbstractFullChangeSelectionTest() {

    @Test
    fun changeReachableMethod() {
        val s = FullChangeSelector(fullCg, setOf(MethodChange(JarTestHelper.CoreA.m)))
        val ea = s.select(listOf(b1.bench1))
        val a = assertSelection(ea)

        Assertions.assertTrue(a.size == 1)
        Assertions.assertTrue(a.contains(b1.bench1))
    }

    @Test
    fun changeNonReachableMethod() {
        val s = FullChangeSelector(fullCg, setOf(MethodChange(JarTestHelper.CoreD.m)))
        val ea = s.select(listOf(b1.bench1))
        val a = assertSelection(ea)
        Assertions.assertTrue(a.isEmpty())
    }

    @Test
    fun changeConstructorReachableMethod() {
        val s1 = FullChangeSelector(fullCg, setOf(MethodChange(JarTestHelper.CoreA.constructor)))

        val ea1 = s1.select(listOf(b1.bench1))
        val a1 = assertSelection(ea1)
        Assertions.assertTrue(a1.size == 1)
        Assertions.assertTrue(a1.contains(b1.bench1))


        val s2 = FullChangeSelector(fullCg, setOf(MethodChange(PlainMethod(
                clazz = JarTestHelper.CoreA.fqn,
                name = "<clinit>",
                params = listOf(),
                returnType = SourceCodeConstants.void
        ))))

        val ea2 = s2.select(listOf(b1.bench1))
        val a2 = assertSelection(ea2)
        Assertions.assertTrue(a2.size == 1)
        Assertions.assertTrue(a2.contains(b1.bench1))
    }

    @Test
    fun changeConstructorNonReachableMethod() {
        val s1 = FullChangeSelector(fullCg, setOf(MethodChange(PlainMethod(
                clazz = JarTestHelper.CoreD.fqn,
                name = "<init>",
                params = listOf(),
                returnType = SourceCodeConstants.void
        ))))

        val ea1 = s1.select(listOf(b1.bench1))
        val a1 = assertSelection(ea1)
        Assertions.assertTrue(a1.isEmpty())


        val s2 = FullChangeSelector(fullCg, setOf(MethodChange(PlainMethod(
                clazz = JarTestHelper.CoreD.fqn,
                name = "<clinit>",
                params = listOf(),
                returnType = SourceCodeConstants.void
        ))))

        val ea2 = s2.select(listOf(b1.bench1))
        val a2 = assertSelection(ea2)
        Assertions.assertTrue(a2.isEmpty())
    }

    private fun changeAffected(c: Change, affected: Boolean) {
        val b = b1.bench1
        val bs = listOf(b)

        // change
        val s1 = FullChangeSelector(fullCg, setOf(c))
        val ea1 = s1.select(bs)
        val a1 = assertSelection(ea1)
        if (affected) {
            Assertions.assertTrue(a1.contains(b))
        } else {
            Assertions.assertTrue(a1.isEmpty())
        }

        // addition
        val s2 = FullChangeSelector(fullCg, setOf(AdditionChange(c)))
        val ea2 = s2.select(bs)
        val a2 = assertSelection(ea2)
        if (affected) {
            Assertions.assertTrue(a2.contains(b))
        } else {
            Assertions.assertTrue(a2.isEmpty())
        }

        // deletion
        val s3 = FullChangeSelector(fullCg, setOf(DeletionChange(c)))
        val ea3 = s3.select(bs)
        val a3 = assertSelection(ea3)
        if (affected) {
            Assertions.assertTrue(a3.contains(b))
        } else {
            Assertions.assertTrue(a3.isEmpty())
        }
    }

    @Test
    fun changeFieldReachableMethod() {
        val cf = ClassFieldChange(
                clazz = Class(name = JarTestHelper.CoreA.fqn),
                field = "someField"
        )
        changeAffected(cf, true)
    }

    @Test
    fun changeFieldNonReachableMethod() {
        val cf = ClassFieldChange(
                clazz = Class(name = JarTestHelper.CoreD.fqn),
                field = "someField"
        )
        changeAffected(cf, false)
    }

    @Test
    fun changeMethodReachableMethod() {
        val m = JarTestHelper.CoreA
        val cm = ClassMethodChange(
                clazz = Class(name = m.fqn),
                method = PlainMethod(clazz = m.fqn, name = "someMethod", params = listOf(), returnType = SourceCodeConstants.void)
        )
        changeAffected(cm, false)
    }

    @Test
    fun changeMethodNonReachableMethod() {
        val m = JarTestHelper.CoreD
        val cm = ClassMethodChange(
                clazz = Class(name = m.fqn),
                method = PlainMethod(clazz = m.fqn, name = "someMethod", params = listOf(), returnType = SourceCodeConstants.void)
        )
        changeAffected(cm, false)
    }

    @Test
    fun changeClassReachableMethod() {
        val m = JarTestHelper.CoreA
        val ch = ClassHeaderChange(clazz = Class(name = m.fqn))
        changeAffected(ch, true)
    }
}
