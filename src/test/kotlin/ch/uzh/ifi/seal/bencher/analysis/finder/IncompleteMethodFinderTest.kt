package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.NoMethod
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.fileResource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class IncompleteMethodFinderTest {

    @Test
    fun noMatch() {
        // non-existent method
        val ne1 = PlainMethod(clazz = "org.sample.core.CoreNE", name = "nonExistentMethod", params = listOf())
        // existing class and name but not with these parameters
        val ne2 = JarTestHelper.BenchParameterized.bench1.copy(params = listOf("java.lang.String"))
        val mf = IncompleteMethodFinder(
                methods = listOf(ne1, ne2),
                jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ems = mf.all()
        if (ems.isLeft()) {
            Assertions.fail<String>("Could not find incomplete methods: ${ems.left().get()}")
        }
        val ms = ems.right().get()

        Assertions.assertEquals(2, ms.size)

        (0 until 2).forEach { Assertions.assertEquals(NoMethod, ms[it]) }
    }

    @Test
    fun matchNoParams() {
        val b = JarTestHelper.BenchParameterized.bench1
        val mf = IncompleteMethodFinder(
                methods = listOf(b),
                jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ems = mf.all()
        if (ems.isLeft()) {
            Assertions.fail<String>("Could not find incomplete methods: ${ems.left().get()}")
        }
        val ms = ems.right().get()

        Assertions.assertEquals(1, ms.size)

        val expected = PlainMethod(
                clazz = b.clazz,
                name = b.name,
                params = b.params
        )
        Assertions.assertEquals(expected, ms[0])
    }

    @Test
    fun match101() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val ne = PlainMethod(clazz = "org.sample.core.CoreNE", name = "nonExistentMethod", params = listOf())
        val b2 = JarTestHelper.BenchNonParameterized.bench2
        val mf = IncompleteMethodFinder(
                methods = listOf(b1, ne, b2),
                jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ems = mf.all()
        if (ems.isLeft()) {
            Assertions.fail<String>("Could not find incomplete methods: ${ems.left().get()}")
        }
        val ms = ems.right().get()

        Assertions.assertEquals(3, ms.size)

        val expected1 = PlainMethod(
                clazz = b1.clazz,
                name = b1.name,
                params = b1.params
        )
        Assertions.assertEquals(expected1, ms[0])

        Assertions.assertEquals(NoMethod, ms[1])

        val expected3 = PlainMethod(
                clazz = b2.clazz,
                name = b2.name,
                params = b2.params
        )
        Assertions.assertEquals(expected3, ms[2])
    }

    @Test
    fun matchFqnParams() {
        val m = JarTestHelper.CoreE.mn2
        val mf = IncompleteMethodFinder(
                methods = listOf(m),
                jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ems = mf.all()
        if (ems.isLeft()) {
            Assertions.fail<String>("Could not find incomplete methods: ${ems.left().get()}")
        }
        val ms = ems.right().get()

        Assertions.assertEquals(1, ms.size)

        val expected = PlainMethod(
                clazz = m.clazz,
                name = m.name,
                params = m.params
        )
        Assertions.assertEquals(expected, ms[0])
    }

    @Test
    fun matchOneParamOnlyClass() {
        val m = JarTestHelper.CoreE.mn_3

        val np = m.params.mapIndexed { i, p ->
            if (i == 1) {
                p.substringAfterLast(".")
            } else {
                p
            }
        }

        val cm = m.copy(params = np)

        val mf = IncompleteMethodFinder(
                methods = listOf(cm),
                jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ems = mf.all()
        if (ems.isLeft()) {
            Assertions.fail<String>("Could not find incomplete methods: ${ems.left().get()}")
        }
        val ms = ems.right().get()

        Assertions.assertEquals(1, ms.size)

        val expected = PlainMethod(
                clazz = m.clazz,
                name = m.name,
                params = m.params
        )
        Assertions.assertEquals(expected, ms[0])
    }

    @Test
    fun matchAllParamOnlyClass() {
        val m = JarTestHelper.CoreE.mn1_2

        val np = m.params.mapIndexed { i, p ->
            p.substringAfterLast(".")
        }

        val cm = m.copy(params = np)

        val mf = IncompleteMethodFinder(
                methods = listOf(cm),
                jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ems = mf.all()
        if (ems.isLeft()) {
            Assertions.fail<String>("Could not find incomplete methods: ${ems.left().get()}")
        }
        val ms = ems.right().get()

        Assertions.assertEquals(1, ms.size)

        val expected = PlainMethod(
                clazz = m.clazz,
                name = m.name,
                params = m.params
        )
        Assertions.assertEquals(expected, ms[0])
    }

    @Test
    fun noMatchOneParamUnknown() {
        val m = JarTestHelper.CoreE.mn_1
        val cm = m.copy(params = listOf("unknown"))

        val mf = IncompleteMethodFinder(
                methods = listOf(cm),
                jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ems = mf.all()
        if (ems.isLeft()) {
            Assertions.fail<String>("Could not find incomplete methods: ${ems.left().get()}")
        }
        val ms = ems.right().get()

        Assertions.assertEquals(1, ms.size)
        Assertions.assertEquals(NoMethod, ms[0])
    }

    @Test
    fun matchOneParamUnknown() {
        val m = JarTestHelper.CoreE.mn_2
        val cm = m.copy(params = listOf("java.lang.String", "unknown"))

        val mf = IncompleteMethodFinder(
                methods = listOf(cm),
                jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ems = mf.all()
        if (ems.isLeft()) {
            Assertions.fail<String>("Could not find incomplete methods: ${ems.left().get()}")
        }
        val ms = ems.right().get()

        Assertions.assertEquals(1, ms.size)
        Assertions.assertEquals(m, ms[0])
    }

    @Test
    fun matchNoMatchOneParamUnknown() {
        val m = JarTestHelper.CoreE.mn1_1
        val cm1 = m.copy(params = listOf("java.lang.String", "unknown"))
        val cm2 = m.copy(params = listOf("unknown", "java.lang.String[]"))

        val mf = IncompleteMethodFinder(
                methods = listOf(cm1, cm2),
                jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ems = mf.all()
        if (ems.isLeft()) {
            Assertions.fail<String>("Could not find incomplete methods: ${ems.left().get()}")
        }
        val ms = ems.right().get()

        Assertions.assertEquals(2, ms.size)
        Assertions.assertEquals(NoMethod, ms[0])
        Assertions.assertEquals(m, ms[1])
    }

    @Test
    fun noMatchTwoParamsUnknown() {
        val m = JarTestHelper.CoreE.mn_2
        val cm = m.copy(params = listOf("unknown", "unknown"))

        val mf = IncompleteMethodFinder(
                methods = listOf(cm),
                jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ems = mf.all()
        if (ems.isLeft()) {
            Assertions.fail<String>("Could not find incomplete methods: ${ems.left().get()}")
        }
        val ms = ems.right().get()

        Assertions.assertEquals(1, ms.size)
        Assertions.assertEquals(NoMethod, ms[0])
    }

    @Test
    fun matchThreeParamsUnknown() {
        val m = JarTestHelper.CoreE.mn2
        val cm = m.copy(params = listOf("unknown", "unknown", "unknown"))

        val mf = IncompleteMethodFinder(
                methods = listOf(cm),
                jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ems = mf.all()
        if (ems.isLeft()) {
            Assertions.fail<String>("Could not find incomplete methods: ${ems.left().get()}")
        }
        val ms = ems.right().get()

        Assertions.assertEquals(1, ms.size)
        Assertions.assertEquals(m, ms[0])
    }
}
