package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.change.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FullChangeSelectorMethodTest : AbstractFullChangeSelectionTest() {

    @Test
    fun changeReachableMethod() {
        val s = FullChangeSelector(fullCg, setOf(MethodChange(JarTestHelper.CoreA.m)))
        val a = s.select(listOf(b1.bench1))
        Assertions.assertTrue(a.toList().size == 1)
        Assertions.assertTrue(a.toList().contains(b1.bench1))
    }

    @Test
    fun changeNonReachableMethod() {
        val s = FullChangeSelector(fullCg, setOf(MethodChange(JarTestHelper.CoreD.m)))
        val a = s.select(listOf(b1.bench1))
        Assertions.assertTrue(a.toList().isEmpty())
    }

    @Test
    fun changeConstructorReachableMethod() {
        val s1 = FullChangeSelector(fullCg, setOf(MethodChange(JarTestHelper.CoreA.constructor)))

        val a1 = s1.select(listOf(b1.bench1))
        Assertions.assertTrue(a1.toList().size == 1)
        Assertions.assertTrue(a1.toList().contains(b1.bench1))


        val s2 = FullChangeSelector(fullCg, setOf(MethodChange(PlainMethod(
                clazz = JarTestHelper.CoreA.fqn,
                name = "<clinit>",
                params = listOf()
        ))))

        val a2 = s2.select(listOf(b1.bench1))
        Assertions.assertTrue(a2.toList().size == 1)
        Assertions.assertTrue(a2.toList().contains(b1.bench1))
    }

    @Test
    fun changeConstructorNonReachableMethod() {
        val s1 = FullChangeSelector(fullCg, setOf(MethodChange(PlainMethod(
                clazz = JarTestHelper.CoreD.fqn,
                name = "<init>",
                params = listOf()
        ))))

        val a1 = s1.select(listOf(b1.bench1))
        Assertions.assertTrue(a1.toList().isEmpty())


        val s2 = FullChangeSelector(fullCg, setOf(MethodChange(PlainMethod(
                clazz = JarTestHelper.CoreD.fqn,
                name = "<clinit>",
                params = listOf()
        ))))

        val a2 = s2.select(listOf(b1.bench1))
        Assertions.assertTrue(a2.toList().isEmpty())
    }

    private fun changeAffected(c: Change, affected: Boolean) {
        val b = b1.bench1
        val bs = listOf(b)

        // change
        val s1 = FullChangeSelector(fullCg, setOf(c))
        val a1 = s1.select(bs)
        if (affected) {
            Assertions.assertTrue(a1.toList().contains(b))
        } else {
            Assertions.assertTrue(a1.toList().isEmpty())
        }

        // addition
        val s2 = FullChangeSelector(fullCg, setOf(AdditionChange(c)))
        val a2 = s2.select(bs)
        if (affected) {
            Assertions.assertTrue(a2.toList().contains(b))
        } else {
            Assertions.assertTrue(a2.toList().isEmpty())
        }

        // deletion
        val s3 = FullChangeSelector(fullCg, setOf(DeletionChange(c)))
        val a3 = s3.select(bs)
        if (affected) {
            Assertions.assertTrue(a3.toList().contains(b))
        } else {
            Assertions.assertTrue(a3.toList().isEmpty())
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
