package ch.uzh.ifi.seal.bencher.analysis.change

import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JarChangeFinderTest {

    @Test
    fun noChanges() {
        val f = JarChangeFinder(pkgPrefix = pkgPrefix)
        val eChanges = f.changes(j1.absoluteFile, j1.absoluteFile)
        if (eChanges.isLeft()) {
            Assertions.fail<String>("Could not get changes: ${eChanges.left().get()}")
        }
        val changes = eChanges.right().get()
        Assertions.assertTrue(changes.isEmpty())
    }

    @Test
    fun changes() {
        val f = JarChangeFinder(pkgPrefix = pkgPrefix)
        val eChanges = f.changes(j1.absoluteFile, j2.absoluteFile)
        if (eChanges.isLeft()) {
            Assertions.fail<String>("Could not get changes: ${eChanges.left().get()}")
        }
        val changes = eChanges.right().get()

        Assertions.assertTrue(changes.size == 5)

        // MethodChange(method=Benchmark(clazz=org.sample.BenchParameterized, name=bench1, params=[], jmhParams=[(str, 1), (str, 2), (str, 3)]))
        val containsB1Change = changes.contains(MethodChange(method = JarTestHelper.BenchParameterized.bench1))
        Assertions.assertTrue(containsB1Change, "No bench1 change")

        // MethodChange(method=PlainMethod(clazz=org.sample.core.CoreA, name=m, params=[]))
        val containsCoreAmChange = changes.contains(MethodChange(method = JarTestHelper.CoreA.m))
        Assertions.assertTrue(containsCoreAmChange, "No CoreA.m change")

        // MethodChange(method=PlainMethod(clazz=org.sample.core.CoreA, name=<init>, params=[java.lang.String, org.sample.core.CoreI]))
        val containsCoreAinitChange = changes.contains(MethodChange(method = JarTestHelper.CoreA.constructor))
        Assertions.assertTrue(containsCoreAinitChange, "No CoreA.<init> change")

        // AdditionChange(type=ClassFieldChange(clazz=Class(file=, name=org.sample.core.CoreA), field=additionalString))
        val addChange = AdditionChange(
                type = ClassFieldChange(
                        clazz = Class(file = "", name = JarTestHelper.CoreA.fqn),
                        field = "additionalString"
                )
        )
        val containsCoreAadditionalStringChange = changes.contains(addChange)
        Assertions.assertTrue(containsCoreAadditionalStringChange, "No CoreA.additionalString change")

        // MethodChange(method=PlainMethod(clazz=org.sample.core.CoreC, name=m, params=[]))
        val containsCoreCmChange = changes.contains(MethodChange(method = JarTestHelper.CoreC.m))
        Assertions.assertTrue(containsCoreCmChange, "No CoreC.m change")
    }

    companion object {
        val pkgPrefix = "org.sample"
        val j1 = JarTestHelper.jar4BenchsJmh121.fileResource()
        val j2 = JarTestHelper.jar4BenchsJmh121v2.fileResource()
    }

}
