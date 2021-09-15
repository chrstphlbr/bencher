package ch.uzh.ifi.seal.bencher.analysis.finder

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import ch.uzh.ifi.seal.bencher.NoMethod
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class IterableMethodFinderTest {
    private fun <T> assertNonError(e: Either<String, List<T>>): List<T> {
        return e.getOrElse {
            Assertions.fail<String>("Could not retrieve methods: ${e.left()}")
            listOf()
        }
    }

    @Test
    fun empty() {
        val mf = IterableMethodFinder(listOf())
        val ms = assertNonError(mf.all())
        Assertions.assertEquals(0, ms.size)
    }

    @Test
    fun noMethodExclude() {
        val mf = IterableMethodFinder(listOf(NoMethod), false)
        val ms = assertNonError(mf.all())
        Assertions.assertEquals(0, ms.size)
    }

    @Test
    fun noMethodInclude() {
        val mf = IterableMethodFinder(listOf(NoMethod), true)
        val ms = assertNonError(mf.all())
        Assertions.assertEquals(1, ms.size)
        Assertions.assertEquals(NoMethod, ms[0])
    }

    @Test
    fun twoMethods() {
        val mf = IterableMethodFinder(listOf(JarTestHelper.CoreE.mn2, NoMethod, JarTestHelper.BenchParameterized.bench1))
        val ms = assertNonError(mf.all())
        Assertions.assertEquals(2, ms.size)
        Assertions.assertEquals(JarTestHelper.CoreE.mn2, ms[0])
        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, ms[1])
    }

    @Test
    fun twoMethodsOneNoMethod() {
        val mf = IterableMethodFinder(listOf(JarTestHelper.CoreE.mn2, NoMethod, JarTestHelper.BenchParameterized.bench1), true)
        val ms = assertNonError(mf.all())
        Assertions.assertEquals(3, ms.size)
        Assertions.assertEquals(JarTestHelper.CoreE.mn2, ms[0])
        Assertions.assertEquals(NoMethod, ms[1])
        Assertions.assertEquals(JarTestHelper.BenchParameterized.bench1, ms[2])
    }
}
