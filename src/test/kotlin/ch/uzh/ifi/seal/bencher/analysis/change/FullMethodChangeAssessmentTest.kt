package ch.uzh.ifi.seal.bencher.analysis.change

import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FullMethodChangeAssessmentTest {

    private val m = PlainMethod(clazz = "A", name = "m", params = listOf(), returnType = "void")

    @Test
    fun noChangeDifferentClass() {
        val c = MethodChange(method = PlainMethod(clazz = "B", name = "m", params = listOf(), returnType = "void"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c),"Change detected although there should not be one")
    }

    @Test
    fun noChangeDifferentMethod() {
        val c = MethodChange(method = PlainMethod(clazz = "A", name = "m2", params = listOf(), returnType = "void"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c),"Change detected although there should not be one")
    }

    @Test
    fun noChangeDifferentParameters() {
        val c = MethodChange(method = PlainMethod(clazz = "A", name = "m", params = listOf("String"), returnType = "void"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c),"Change detected although there should not be one")
    }

    @Test
    fun noChangeDifferentReturnType() {
        val c = MethodChange(method = PlainMethod(clazz = "A", name = "m", params = listOf(), returnType = "String"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c),"Change detected although there should not be one")
    }

    @Test
    fun changeMethod() {
        val c = MethodChange(method = PlainMethod(clazz = "A", name = "m", params = listOf(), returnType = "void"))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c),"No change detected although there should be one")
    }

    @Test
    fun classHeader() {
        // change
        val c1 = ClassHeaderChange(clazz = Class(name = "A"))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        // no change
        val c2 = ClassHeaderChange(clazz = Class(name = "B"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c2),"Change detected although there should not be one")
    }

    @Test
    fun classField() {
        // change
        val c1 = ClassFieldChange(clazz = Class(name = "A"), field = "f")
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        // no change
        val c2 = ClassFieldChange(clazz = Class(name = "B"), field = "f")
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c2),"Change detected although there should not be one")
    }

    @Test
    fun classMethod() {
        // change
        val c1 = ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "m", params = listOf(), returnType = "void"))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        //no change
        val c2 = ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "m", params = listOf(), returnType = "void"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c2),"Change detected although there should not be one")
    }

    @Test
    fun classMethodConstructor() {
        // change

        val c1 = ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "<init>", params = listOf(), returnType = "void"))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        val c2 = ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "<init>", params = listOf("String"), returnType = "void"))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c2),"No change detected although there should be one")

        val c3 = ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "<init>", params = listOf("String", "int", "float64"), returnType = "void"))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c3),"No change detected although there should be one")

        // no change

        val c4 = ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "<init>", params = listOf(), returnType = "void"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c4),"Change detected although there should not be one")

        val c5 = ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "<init>", params = listOf("String"), returnType = "void"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c5),"Change detected although there should not be one")

        val c6 = ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "<init>", params = listOf("String", "int", "float64"), returnType = "void"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c6),"Change detected although there should not be one")
    }

    @Test
    fun classMethodStaticInitializer() {
        // change

        val c1 = ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "<clinit>", params = listOf(), returnType = "void"))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        // no change

        val c2 = ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "<clinit>", params = listOf(), returnType = "void"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c2),"Change detected although there should not be one")
    }

    @Test
    fun classMethodJMHSetup() {
        // change

        val c1 = ClassMethodChange(clazz = Class(name = "A"), method = SetupMethod(clazz = "A", name = "setup", params = listOf(), returnType = "void"))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        val c2 = ClassMethodChange(clazz = Class(name = "A"), method = SetupMethod(clazz = "A", name = "alaksjfd", params = listOf(), returnType = "void"))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c2),"No change detected although there should be one")

        // no change

        val c3 = ClassMethodChange(clazz = Class(name = "B"), method = SetupMethod(clazz = "B", name = "setup", params = listOf(), returnType = "void"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c3),"Change detected although there should not be one")

        val c4 = ClassMethodChange(clazz = Class(name = "B"), method = SetupMethod(clazz = "B", name = "alaksjfd", params = listOf(), returnType = "void"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c4),"Change detected although there should not be one")
    }

    @Test
    fun classMethodJMHTearDown() {
        // change

        val c1 = ClassMethodChange(clazz = Class(name = "A"), method = TearDownMethod(clazz = "A", name = "teardown", params = listOf(), returnType = "void"))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        val c2 = ClassMethodChange(clazz = Class(name = "A"), method = TearDownMethod(clazz = "A", name = "aslfk", params = listOf(), returnType = "void"))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c2),"No change detected although there should be one")

        // no change

        val c3 = ClassMethodChange(clazz = Class(name = "B"), method = TearDownMethod(clazz = "B", name = "teardown", params = listOf(), returnType = "void"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c3),"Change detected although there should not be one")

        val c4 = ClassMethodChange(clazz = Class(name = "B"), method = TearDownMethod(clazz = "B", name = "aslfk", params = listOf(), returnType = "void"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c4),"Change detected although there should not be one")
    }


    // DeletionChange


    @Test
    fun deletionNoChangeDifferentClass() {
        val c = DeletionChange(MethodChange(method = PlainMethod(clazz = "B", name = "m", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c),"Change detected although there should not be one")
    }

    @Test
    fun deletionNoChangeDifferentMethod() {
        val c = DeletionChange(MethodChange(method = PlainMethod(clazz = "A", name = "m2", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c),"Change detected although there should not be one")
    }

    @Test
    fun deletionNoChangeDifferentParameters() {
        val c = DeletionChange(MethodChange(method = PlainMethod(clazz = "A", name = "m", params = listOf("String"), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c),"Change detected although there should not be one")
    }

    @Test
    fun deletionNoChangeDifferentReturnType() {
        val c = DeletionChange(MethodChange(method = PlainMethod(clazz = "A", name = "m", params = listOf(), returnType = "String")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c),"Change detected although there should not be one")
    }

    @Test
    fun deletionChangeMethod() {
        val c = DeletionChange(MethodChange(method = PlainMethod(clazz = "A", name = "m", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c),"No change detected although there should be one")
    }

    @Test
    fun deletionClassHeader() {
        // change
        val c1 = DeletionChange(ClassHeaderChange(clazz = Class(name = "A")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        // no change
        val c2 = DeletionChange(ClassHeaderChange(clazz = Class(name = "B")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c2),"Change detected although there should not be one")
    }

    @Test
    fun deletionClassField() {
        // change
        val c1 = DeletionChange(ClassFieldChange(clazz = Class(name = "A"), field = "f"))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        // no change
        val c2 = DeletionChange(ClassFieldChange(clazz = Class(name = "B"), field = "f"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c2),"Change detected although there should not be one")
    }

    @Test
    fun deletionClassMethod() {
        // change
        val c1 = DeletionChange(ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "m", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        //no change
        val c2 = DeletionChange(ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "m", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c2),"Change detected although there should not be one")
    }

    @Test
    fun deletionClassMethodConstructor() {
        // change

        val c1 = DeletionChange(ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "<init>", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        val c2 = DeletionChange(ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "<init>", params = listOf("String"), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c2),"No change detected although there should be one")

        val c3 = DeletionChange(ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "<init>", params = listOf("String", "int", "float64"), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c3),"No change detected although there should be one")

        // no change

        val c4 = DeletionChange(ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "<init>", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c4),"Change detected although there should not be one")

        val c5 = DeletionChange(ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "<init>", params = listOf("String"), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c5),"Change detected although there should not be one")

        val c6 = DeletionChange(ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "<init>", params = listOf("String", "int", "float64"), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c6),"Change detected although there should not be one")
    }

    @Test
    fun deletionClassMethodStaticInitializer() {
        // change

        val c1 = DeletionChange(ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "<clinit>", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        // no change

        val c2 = DeletionChange(ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "<clinit>", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c2),"Change detected although there should not be one")
    }

    @Test
    fun deletionClassMethodJMHSetup() {
        // change

        val c1 = DeletionChange(ClassMethodChange(clazz = Class(name = "A"), method = SetupMethod(clazz = "A", name = "setup", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        val c2 = DeletionChange(ClassMethodChange(clazz = Class(name = "A"), method = SetupMethod(clazz = "A", name = "alaksjfd", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c2),"No change detected although there should be one")

        // no change

        val c3 = DeletionChange(ClassMethodChange(clazz = Class(name = "B"), method = SetupMethod(clazz = "B", name = "setup", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c3),"Change detected although there should not be one")

        val c4 = DeletionChange(ClassMethodChange(clazz = Class(name = "B"), method = SetupMethod(clazz = "B", name = "alaksjfd", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c4),"Change detected although there should not be one")
    }

    @Test
    fun deletionClassMethodJMHTearDown() {
        // change

        val c1 = DeletionChange(ClassMethodChange(clazz = Class(name = "A"), method = TearDownMethod(clazz = "A", name = "teardown", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        val c2 = DeletionChange(ClassMethodChange(clazz = Class(name = "A"), method = TearDownMethod(clazz = "A", name = "aslfk", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c2),"No change detected although there should be one")

        // no change

        val c3 = DeletionChange(ClassMethodChange(clazz = Class(name = "B"), method = TearDownMethod(clazz = "B", name = "teardown", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c3),"Change detected although there should not be one")

        val c4 = DeletionChange(ClassMethodChange(clazz = Class(name = "B"), method = TearDownMethod(clazz = "B", name = "aslfk", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c4),"Change detected although there should not be one")
    }


    // AdditionChange


    @Test
    fun additionNoChangeDifferentClass() {
        val c = AdditionChange(MethodChange(method = PlainMethod(clazz = "B", name = "m", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c),"Change detected although there should not be one")
    }

    @Test
    fun additionNoChangeDifferentMethod() {
        val c = AdditionChange(MethodChange(method = PlainMethod(clazz = "A", name = "m2", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c),"Change detected although there should not be one")
    }

    @Test
    fun additionNoChangeDifferentParameters() {
        val c = AdditionChange(MethodChange(method = PlainMethod(clazz = "A", name = "m", params = listOf("String"), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c),"Change detected although there should not be one")
    }

    @Test
    fun additionNoChangeDifferentReturnType() {
        val c = AdditionChange(MethodChange(method = PlainMethod(clazz = "A", name = "m", params = listOf(), returnType = "String")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c),"Change detected although there should not be one")
    }

    @Test
    fun additionChangeMethod() {
        val c = AdditionChange(MethodChange(method = PlainMethod(clazz = "A", name = "m", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c),"No change detected although there should be one")
    }

    @Test
    fun additionClassHeader() {
        // change
        val c1 = AdditionChange(ClassHeaderChange(clazz = Class(name = "A")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        // no change
        val c2 = AdditionChange(ClassHeaderChange(clazz = Class(name = "B")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c2),"Change detected although there should not be one")
    }

    @Test
    fun additionClassField() {
        // change
        val c1 = AdditionChange(ClassFieldChange(clazz = Class(name = "A"), field = "f"))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        // no change
        val c2 = AdditionChange(ClassFieldChange(clazz = Class(name = "B"), field = "f"))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c2),"Change detected although there should not be one")
    }

    @Test
    fun additionClassMethod() {
        // change
        val c1 = AdditionChange(ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "m", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        //no change
        val c2 = AdditionChange(ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "m", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c2),"Change detected although there should not be one")
    }

    @Test
    fun additionClassMethodConstructor() {
        // change

        val c1 = AdditionChange(ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "<init>", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        val c2 = AdditionChange(ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "<init>", params = listOf("String"), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c2),"No change detected although there should be one")

        val c3 = AdditionChange(ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "<init>", params = listOf("String", "int", "float64"), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c3),"No change detected although there should be one")

        // no change

        val c4 = AdditionChange(ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "<init>", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c4),"Change detected although there should not be one")

        val c5 = AdditionChange(ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "<init>", params = listOf("String"), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c5),"Change detected although there should not be one")

        val c6 = AdditionChange(ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "<init>", params = listOf("String", "int", "float64"), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c6),"Change detected although there should not be one")
    }

    @Test
    fun additionClassMethodStaticInitializer() {
        // change

        val c1 = AdditionChange(ClassMethodChange(clazz = Class(name = "A"), method = PlainMethod(clazz = "A", name = "<clinit>", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        // no change

        val c2 = AdditionChange(ClassMethodChange(clazz = Class(name = "B"), method = PlainMethod(clazz = "B", name = "<clinit>", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c2),"Change detected although there should not be one")
    }

    @Test
    fun additionClassMethodJMHSetup() {
        // change

        val c1 = AdditionChange(ClassMethodChange(clazz = Class(name = "A"), method = SetupMethod(clazz = "A", name = "setup", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        val c2 = AdditionChange(ClassMethodChange(clazz = Class(name = "A"), method = SetupMethod(clazz = "A", name = "alaksjfd", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c2),"No change detected although there should be one")

        // no change

        val c3 = AdditionChange(ClassMethodChange(clazz = Class(name = "B"), method = SetupMethod(clazz = "B", name = "setup", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c3),"Change detected although there should not be one")

        val c4 = AdditionChange(ClassMethodChange(clazz = Class(name = "B"), method = SetupMethod(clazz = "B", name = "alaksjfd", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c4),"Change detected although there should not be one")
    }

    @Test
    fun additionClassMethodJMHTearDown() {
        // change

        val c1 = AdditionChange(ClassMethodChange(clazz = Class(name = "A"), method = TearDownMethod(clazz = "A", name = "teardown", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c1),"No change detected although there should be one")

        val c2 = AdditionChange(ClassMethodChange(clazz = Class(name = "A"), method = TearDownMethod(clazz = "A", name = "aslfk", params = listOf(), returnType = "void")))
        assertTrue(FullMethodChangeAssessment.methodChanged(m, c2),"No change detected although there should be one")

        // no change

        val c3 = AdditionChange(ClassMethodChange(clazz = Class(name = "B"), method = TearDownMethod(clazz = "B", name = "teardown", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c3),"Change detected although there should not be one")

        val c4 = AdditionChange(ClassMethodChange(clazz = Class(name = "B"), method = TearDownMethod(clazz = "B", name = "aslfk", params = listOf(), returnType = "void")))
        assertFalse(FullMethodChangeAssessment.methodChanged(m, c4),"Change detected although there should not be one")
    }
}
