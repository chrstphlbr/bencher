package ch.uzh.ifi.seal.bencher.analysis.finder

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.NoMethod
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.AccessModifier
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import ch.uzh.ifi.seal.bencher.analysis.sourceCode
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.classLoader.IMethod
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class IncompleteMethodFinderTest {

    private fun assertBencherMethods(size: Int, bencherMethods: Either<String, List<Method>>, bencherWalaMethods: Either<String, List<Pair<Method, IMethod?>>>) {
        val bms = bencherMethods.getOrElse {
            Assertions.fail<String>("Could not find incomplete methods (all): ${bencherMethods.left()}")
            listOf()
        }
        val bwms = bencherWalaMethods.getOrElse {
            Assertions.fail<String>("Could not find incomplete methods (bencherWalaMethods): ${bencherWalaMethods.left()}")
            listOf()
        }

        Assertions.assertEquals(size, bms.size)
        Assertions.assertEquals(size, bwms.size)

        // assert that bencherMethods and bencherWalaMethods (first element) are equal
        bms.zip(bwms).forEach { (bm, bwm) ->
            Assertions.assertEquals(bm, bwm.first)
        }
    }

    private fun assertIMethod(expected: Method, im: IMethod?) {
        Assertions.assertNotNull(im)
        Assertions.assertNotNull(im!!.reference)
        Assertions.assertNotNull(im.descriptor)
        val r = im.reference
        // assert class
        Assertions.assertEquals(expected.clazz, r.declaringClass.name.toUnicodeString().sourceCode)

        // assert name
        Assertions.assertEquals(expected.name, r.name.toUnicodeString())

        // assert params
        Assertions.assertEquals(expected.params.size, r.numberOfParameters)
        if (expected.params.isNotEmpty()) {
            expected.params.zip(r.descriptor.parameters).forEach { (ep, ap) ->
                Assertions.assertEquals(ep, ap.toUnicodeString().sourceCode)
            }
        }
    }

    @Test
    fun noMatch() {
        // non-existent method
        val ne1 = PlainMethod(
            clazz = "org.sample.core.CoreNE",
            name = "nonExistentMethod",
            params = listOf(),
            returnType = SourceCodeConstants.void
        )
        // existing class and name but not with these parameters
        val ne2 = JarTestHelper.BenchParameterized.bench1.copy(params = listOf("java.lang.String"))
        val mf = IncompleteMethodFinder(
            methods = listOf(ne1, ne2),
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ebms = mf.all()
        val ebwms = mf.bencherWalaMethods()
        assertBencherMethods(2, ebms, ebwms)

        val ms = ebwms.getOrElse {
            throw IllegalStateException("should never happen")
        }

        (0 until 2).forEach {
            Assertions.assertEquals(NoMethod, ms[it].first)
            Assertions.assertNull(ms[it].second)
        }
    }

    @Test
    fun matchConstructor() {
        val c = JarTestHelper.BenchParameterized2v2.constructor
        val mf = IncompleteMethodFinder(
            methods = listOf(c),
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ebms = mf.all()
        val ebwms = mf.bencherWalaMethods()
        assertBencherMethods(1, ebms, ebwms)

        val ms = ebwms.getOrElse {
            throw IllegalStateException("should never happen")
        }

        val expected = c.toPlainMethod()
        Assertions.assertEquals(expected, ms[0].first)
        assertIMethod(expected, ms[0].second)
    }

    @Test
    fun matchNoParams() {
        val b = JarTestHelper.BenchParameterized.bench1
        val mf = IncompleteMethodFinder(
            methods = listOf(b),
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ebms = mf.all()
        val ebwms = mf.bencherWalaMethods()
        assertBencherMethods(1, ebms, ebwms)

        val ms = ebwms.getOrElse {
            throw IllegalStateException("should never happen")
        }

        val expected = b.toPlainMethod()
        Assertions.assertEquals(expected, ms[0].first)
        assertIMethod(expected, ms[0].second)
    }

    @Test
    fun match101() {
        val b1 = JarTestHelper.BenchParameterized.bench1
        val ne = PlainMethod(
            clazz = "org.sample.core.CoreNE",
            name = "nonExistentMethod",
            params = listOf(),
            returnType = SourceCodeConstants.void
        )
        val b2 = JarTestHelper.BenchNonParameterized.bench2
        val mf = IncompleteMethodFinder(
            methods = listOf(b1, ne, b2),
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ebms = mf.all()
        val ebwms = mf.bencherWalaMethods()
        assertBencherMethods(3, ebms, ebwms)

        val ms = ebwms.getOrElse {
            throw IllegalStateException("should never happen")
        }

        val expected1 = b1.toPlainMethod()
        Assertions.assertEquals(expected1, ms[0].first)
        assertIMethod(expected1, ms[0].second)

        Assertions.assertEquals(NoMethod, ms[1].first)
        Assertions.assertNull(ms[1].second)

        val expected3 = PlainMethod(
            clazz = b2.clazz,
            name = b2.name,
            params = b2.params,
            returnType = SourceCodeConstants.void
        )
        Assertions.assertEquals(expected3, ms[2].first)
        assertIMethod(expected3, ms[2].second)
    }

    @Test
    fun matchFqnParams() {
        val m = JarTestHelper.CoreE.mn2
        val mf = IncompleteMethodFinder(
            methods = listOf(m),
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ebms = mf.all()
        val ebwms = mf.bencherWalaMethods()
        assertBencherMethods(1, ebms, ebwms)

        val ms = ebwms.getOrElse {
            throw IllegalStateException("should never happen")
        }

        val expected = m.toPlainMethod()
        Assertions.assertEquals(expected, ms[0].first)
        assertIMethod(expected, ms[0].second)
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

        val ebms = mf.all()
        val ebwms = mf.bencherWalaMethods()
        assertBencherMethods(1, ebms, ebwms)

        val ms = ebwms.getOrElse {
            throw IllegalStateException("should never happen")
        }

        val expected = PlainMethod(
            clazz = m.clazz,
            name = m.name,
            params = m.params,
            returnType = SourceCodeConstants.void
        )
        Assertions.assertEquals(expected, ms[0].first)
        assertIMethod(expected, ms[0].second)
    }

    @Test
    fun matchAllParamOnlyClass() {
        val m = JarTestHelper.CoreE.mn1_2

        val np = m.params.map { p ->
            p.substringAfterLast(".")
        }

        val cm = m.copy(params = np)

        val mf = IncompleteMethodFinder(
            methods = listOf(cm),
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ebms = mf.all()
        val ebwms = mf.bencherWalaMethods()
        assertBencherMethods(1, ebms, ebwms)

        val ms = ebwms.getOrElse {
            throw IllegalStateException("should never happen")
        }

        val expected = m.toPlainMethod()
        Assertions.assertEquals(expected, ms[0].first)
        assertIMethod(expected, ms[0].second)
    }

    @Test
    fun noMatchOneParamUnknown() {
        val m = JarTestHelper.CoreE.mn_1
        val cm = m.copy(params = listOf("unknown"))

        val mf = IncompleteMethodFinder(
            methods = listOf(cm),
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ebms = mf.all()
        val ebwms = mf.bencherWalaMethods()
        assertBencherMethods(1, ebms, ebwms)

        val ms = ebwms.getOrElse {
            throw IllegalStateException("should never happen")
        }
        Assertions.assertEquals(NoMethod, ms[0].first)
        Assertions.assertNull(ms[0].second)
    }

    @Test
    fun matchOneParamUnknown() {
        val m = JarTestHelper.CoreE.mn_2
        val cm = m.copy(params = listOf("java.lang.String", "unknown"))

        val mf = IncompleteMethodFinder(
            methods = listOf(cm),
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ebms = mf.all()
        val ebwms = mf.bencherWalaMethods()
        assertBencherMethods(1, ebms, ebwms)

        val ms = ebwms.getOrElse {
            throw IllegalStateException("should never happen")
        }
        Assertions.assertEquals(m, ms[0].first)
        assertIMethod(m, ms[0].second)
    }

    @Test
    fun matchOneParamUnknownOnlyPublic() {
        val m = JarTestHelper.CoreE.mn1_1
        val cm1 = m.copy(params = listOf("java.lang.String", "unknown"))
        val cm2 = m.copy(params = listOf("unknown", "java.lang.String[]"))

        val mf = IncompleteMethodFinder(
            methods = listOf(cm1, cm2),
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath(),
            acceptedAccessModifier = setOf(AccessModifier.PUBLIC)
        )

        val ebms = mf.all()
        val ebwms = mf.bencherWalaMethods()
        assertBencherMethods(2, ebms, ebwms)

        val ms = ebwms.getOrElse {
            throw IllegalStateException("should never happen")
        }
        Assertions.assertEquals(m, ms[0].first)
        assertIMethod(m, ms[0].second)
        Assertions.assertEquals(m, ms[1].first)
        assertIMethod(m, ms[1].second)
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

        val ebms = mf.all()
        val ebwms = mf.bencherWalaMethods()
        assertBencherMethods(2, ebms, ebwms)

        val ms = ebwms.getOrElse {
            throw IllegalStateException("should never happen")
        }
        Assertions.assertEquals(NoMethod, ms[0].first)
        Assertions.assertNull(ms[0].second)
        Assertions.assertEquals(m, ms[1].first)
        assertIMethod(m, ms[1].second)
    }

    @Test
    fun noMatchTwoParamsUnknown() {
        val m = JarTestHelper.CoreE.mn_2
        val cm = m.copy(params = listOf("unknown", "unknown"))

        val mf = IncompleteMethodFinder(
            methods = listOf(cm),
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ebms = mf.all()
        val ebwms = mf.bencherWalaMethods()
        assertBencherMethods(1, ebms, ebwms)

        val ms = ebwms.getOrElse {
            throw IllegalStateException("should never happen")
        }
        Assertions.assertEquals(NoMethod, ms[0].first)
        Assertions.assertNull(ms[0].second)
    }

    @Test
    fun matchThreeParamsUnknown() {
        val m = JarTestHelper.CoreE.mn2
        val cm = m.copy(params = listOf("unknown", "unknown", "unknown"))

        val mf = IncompleteMethodFinder(
            methods = listOf(cm),
            jar = JarTestHelper.jar4BenchsJmh121v2.fileResource().toPath()
        )

        val ebms = mf.all()
        val ebwms = mf.bencherWalaMethods()
        assertBencherMethods(1, ebms, ebwms)

        val ms = ebwms.getOrElse {
            throw IllegalStateException("should never happen")
        }
        Assertions.assertEquals(m, ms[0].first)
        assertIMethod(m, ms[0].second)
    }
}
