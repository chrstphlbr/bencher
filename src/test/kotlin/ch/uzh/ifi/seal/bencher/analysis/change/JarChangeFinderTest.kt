package ch.uzh.ifi.seal.bencher.analysis.change

import arrow.core.getOrElse
import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JarChangeFinderTest {

    private fun nonJMHGenerated(s: String): Boolean =
            !s.contains("generated")

    private fun nonJMHGenerated(c: Change): Boolean = when (c) {
        is MethodChange -> nonJMHGenerated(c.method.clazz)
        is ClassHeaderChange -> nonJMHGenerated(c.clazz.name)
        is ClassFieldChange -> nonJMHGenerated(c.clazz.name)
        is ClassMethodChange -> nonJMHGenerated(c.clazz.name)
        is DeletionChange -> nonJMHGenerated(c.type)
        is AdditionChange -> nonJMHGenerated(c.type)
        is LineChange -> nonJMHGenerated(c.line.file)
    }


    private fun filterJMHGeneratedChanges(cs: Iterable<Change>) =
            cs.filter { nonJMHGenerated(it) }

    @Test
    fun noChanges() {
        val f = JarChangeFinder(pkgPrefixes = pkgPrefixes)
        val changes = f.changes(j1.absoluteFile, j1.absoluteFile).getOrElse {
            Assertions.fail<String>("Could not get changes: $it")
            return
        }
        Assertions.assertTrue(changes.isEmpty())
    }

    @Test
    fun changes() {
        val f = JarChangeFinder(pkgPrefixes = pkgPrefixes)
        val allChanges = f.changes(j1.absoluteFile, j2.absoluteFile).getOrElse {
            Assertions.fail<String>("Could not get changes: $it")
            return
        }

        // filter JMH-generated changes
        val changes = filterJMHGeneratedChanges(allChanges)

        Assertions.assertEquals(20, changes.size)

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
                clazz = Class(name = JarTestHelper.CoreA.fqn),
                field = "additionalString"
            )
        )
        val containsCoreAadditionalStringChange = changes.contains(addChange)
        Assertions.assertTrue(containsCoreAadditionalStringChange, "No CoreA.additionalString change")

        // MethodChange(method=PlainMethod(clazz=org.sample.core.CoreC, name=m, params=[]))
        val containsCoreCmChange = changes.contains(MethodChange(method = JarTestHelper.CoreC.m))
        Assertions.assertTrue(containsCoreCmChange, "No CoreC.m change")


        // AdditionChange(type=ClassHeaderChange(clazz=Class(name=org.sample.core.CoreE)))
        val addChangeCoreE = AdditionChange(type = ClassHeaderChange(clazz = Class(name = JarTestHelper.CoreE.fqn)))
        val containsNewCoreE = changes.contains(addChangeCoreE)
        Assertions.assertTrue(containsNewCoreE, "No CoreE addition change")

        // AdditionChange(type=ClassHeaderChange(clazz=Class(name=org.sample.NestedBenchmark$Bench1)))
        val addChangeB1 =
            AdditionChange(type = ClassHeaderChange(clazz = Class(name = JarTestHelper.NestedBenchmark.Bench1.fqn)))
        val containsNewB1 = changes.contains(addChangeB1)
        Assertions.assertTrue(containsNewB1, "No NestedBenchmark.Bench1 addition change")

        // AdditionChange(type=ClassHeaderChange(clazz=Class(name=org.sample.NestedBenchmark$Bench3$Bench32)))
        val addChangeB32 =
            AdditionChange(type = ClassHeaderChange(clazz = Class(name = JarTestHelper.NestedBenchmark.Bench3.Bench32.fqn)))
        val containsNewB32 = changes.contains(addChangeB32)
        Assertions.assertTrue(containsNewB32, "No NestedBenchmark.Bench3.Bench32 addition change")

        // AdditionChange(type=ClassHeaderChange(clazz=Class(name=org.sample.NestedBenchmark$Bench3)))
        val addChangeB3 =
            AdditionChange(type = ClassHeaderChange(clazz = Class(name = JarTestHelper.NestedBenchmark.Bench3.fqn)))
        val containsNewB3 = changes.contains(addChangeB3)
        Assertions.assertTrue(containsNewB3, "No NestedBenchmark.Bench3 addition change")


        // AdditionChange(type=ClassHeaderChange(clazz=Class(name=org.sample.NestedBenchmark)))
        val addChangeNB =
            AdditionChange(type = ClassHeaderChange(clazz = Class(name = JarTestHelper.NestedBenchmark.fqn)))
        val containsNewNB = changes.contains(addChangeNB)
        Assertions.assertTrue(containsNewNB, "No NestedBenchmark addition change")


        // changes to BenchParameterized2

        // MethodChange(method=PlainMethod(clazz=org.sample.BenchParameterized2, name=<init>, params=[]))
        val containsB4initChange =
            changes.contains(MethodChange(method = JarTestHelper.BenchParameterized2v2.constructor))
        Assertions.assertTrue(containsB4initChange, "No bench4.<init> change")

        // DeletionChange(type=ClassMethodChange(clazz=Class(name=org.sample.BenchParameterized2), method=Benchmark(clazz=org.sample.BenchParameterized2, name=bench4, params=[], jmhParams=[(str, 1), (str, 2), (str, 3)])))
        val containsB4SigDel = changes.contains(
            DeletionChange(
                type = ClassMethodChange(
                    clazz = Class(name = JarTestHelper.BenchParameterized2.fqn),
                    method = JarTestHelper.BenchParameterized2.bench4
                )
            )
        )
        Assertions.assertTrue(containsB4SigDel, "No bench4 signature deletion change")

        // DeletionChange(type=MethodChange(method=Benchmark(clazz=org.sample.BenchParameterized2, name=bench4, params=[], jmhParams=[(str, 1), (str, 2), (str, 3)])))
        val containsB4BodyDel =
            changes.contains(DeletionChange(type = MethodChange(method = JarTestHelper.BenchParameterized2.bench4)))
        Assertions.assertTrue(containsB4BodyDel, "No bench4 body deletion change")

        // AdditionChange(type=ClassFieldChange(clazz=Class(name=org.sample.BenchParameterized2), field=str2))
        val containsB4str2Add = changes.contains(
            AdditionChange(
                type = ClassFieldChange(
                    clazz = Class(name = JarTestHelper.BenchParameterized2v2.fqn),
                    field = "str2"
                )
            )
        )
        Assertions.assertTrue(containsB4str2Add, "No BenchParameterized2 instance variable ('str2') addition change")

        // AdditionChange(type=ClassMethodChange(clazz=Class(name=org.sample.BenchParameterized2), method=Benchmark(clazz=org.sample.BenchParameterized2, name=bench4, params=[], jmhParams=[(str, 1), (str, 2), (str, 3), (str2, 1), (str2, 2), (str2, 3)])))
        val containsB4SigAdd = changes.contains(
            AdditionChange(
                type = ClassMethodChange(
                    clazz = Class(name = JarTestHelper.BenchParameterized2v2.fqn),
                    method = JarTestHelper.BenchParameterized2v2.bench4
                )
            )
        )
        Assertions.assertTrue(containsB4SigAdd, "No bench4 signature addition change")

        // AdditionChange(type=MethodChange(method=Benchmark(clazz=org.sample.BenchParameterized2, name=bench4, params=[], jmhParams=[(str, 1), (str, 2), (str, 3), (str2, 1), (str2, 2), (str2, 3)])))
        val containsB4BodyAdd =
            changes.contains(AdditionChange(type = MethodChange(method = JarTestHelper.BenchParameterized2v2.bench4)))
        Assertions.assertTrue(containsB4BodyAdd, "No bench4 body addition change")


        // AdditionChange(type=ClassHeaderChange(clazz=Class(name=org.sample.BenchsWithGroup)))
        val addChangeGroup =
            AdditionChange(type = ClassHeaderChange(clazz = Class(name = JarTestHelper.BenchsWithGroup.fqn)))
        val containsNewGroup = changes.contains(addChangeGroup)
        Assertions.assertTrue(containsNewGroup, "No BenchsWithGroup addition change")

        // AdditionChange(type=ClassHeaderChange(clazz=Class(name=org.sample.BenchsWithStateObj)))
        val addChangeStateObj =
            AdditionChange(type = ClassHeaderChange(clazz = Class(name = JarTestHelper.BenchsStateObj.fqn)))
        val containsStateObj = changes.contains(addChangeStateObj)
        Assertions.assertTrue(containsStateObj, "No BenchsWithStateObj addition change")

        // AdditionChange(type=ClassHeaderChange(clazz=Class(name=org.sample.stateObj.ObjectA)))
        val addChangeObjectA = AdditionChange(type = ClassHeaderChange(clazz = Class(name = JarTestHelper.ObjectA.fqn)))
        val containsObjectA = changes.contains(addChangeObjectA)
        Assertions.assertTrue(containsObjectA, "No ObjectA addition change")

        // AdditionChange(type=ClassHeaderChange(clazz=Class(name=org.sample.stateObj.ObjectB)))
        val addChangeObjectB = AdditionChange(type = ClassHeaderChange(clazz = Class(name = JarTestHelper.ObjectB.fqn)))
        val containsObjectB = changes.contains(addChangeObjectB)
        Assertions.assertTrue(containsObjectB, "No ObjectB addition change")
    }

    companion object {
        val pkgPrefixes = setOf("org.sample", "org.sam")
        val j1 = JarTestHelper.jar4BenchsJmh121.fileResource()
        val j2 = JarTestHelper.jar4BenchsJmh121v2.fileResource()
    }

}
