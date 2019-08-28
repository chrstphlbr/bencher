package ch.uzh.ifi.seal.bencher

import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MethodComparatorTest {

    @Test
    fun noMethodEqual() {
        val c = MethodComparator.compare(NoMethod, NoMethod)
        Assertions.assertEquals(0, c, "NoMethod and NoMethod incorrect")
    }

    @Test
    fun noMethodEqualEmptyMethod() {
        val o = PlainMethod(
                clazz = "",
                name = "",
                params = listOf(),
                returnType = SourceCodeConstants.void
        )
        val c1 = MethodComparator.compare(NoMethod, o)
        Assertions.assertEquals(0, c1, "NoMethod and PlainMethod incorrect")

        val c2 = MethodComparator.compare(o, NoMethod)
        Assertions.assertEquals(0, c2, "PlainMethod and NoMethod incorrect")
    }

    @Test
    fun plainMethodsEqual() {
        val params = (1..10).map { "a" }
        val m1 = PlainMethod(
                clazz = "aaa",
                name = "aaa",
                params = params,
                returnType = SourceCodeConstants.void
        )
        val m2 = PlainMethod(
                clazz = "aaa",
                name = "aaa",
                params = params,
                returnType = SourceCodeConstants.void
        )
        val c1 = MethodComparator.compare(m1, m2)
        Assertions.assertEquals(0, c1, "$m1 != $m2")

        val c2 = MethodComparator.compare(m2, m1)
        Assertions.assertEquals(0, c2, "$m2 != $m1")
    }

    @Test
    fun plainMethodsUnequalClass() {
        val m1 = PlainMethod(
                clazz = "aaa",
                name = "aaa",
                params = listOf(),
                returnType = SourceCodeConstants.void
        )
        val m2 = PlainMethod(
                clazz = "aabAssertions.assertTrue(c2 > 0",
                name = "aaa",
                params = listOf(),
                returnType = SourceCodeConstants.void
        )
        val c1 = MethodComparator.compare(m1, m2)
        Assertions.assertTrue(c1 < 0, "$m1 !< $m2")

        val c2 = MethodComparator.compare(m2, m1)
        Assertions.assertTrue(c2 > 0, "$m2 !> $m1")
    }

    @Test
    fun plainMethodsUnequalName() {
        val m1 = PlainMethod(
                clazz = "aaa",
                name = "aaa",
                params = listOf(),
                returnType = SourceCodeConstants.void
        )
        val m2 = PlainMethod(
                clazz = "aaa",
                name = "aab",
                params = listOf(),
                returnType = SourceCodeConstants.void
        )
        val c1 = MethodComparator.compare(m1, m2)
        Assertions.assertTrue(c1 < 0, "$m1 !< $m2")

        val c2 = MethodComparator.compare(m2, m1)
        Assertions.assertTrue(c2 > 0, "$m2 !> $m1")
    }

    private fun paramsUnequalType(pos: Int, size: Int): Pair<Method, Method> {
        val a = 97
        val params = (0 until size).map { (a + it).toChar().toString() }

        val m1 = PlainMethod(
                clazz = "aaa",
                name = "aaa",
                params = params,
                returnType = SourceCodeConstants.void
        )
        val m2 = PlainMethod(
                clazz = "aaa",
                name = "aaa",
                params = params.mapIndexed { i, v ->
                    if (i + 1 == pos) {
                        "z"
                    } else {
                        v
                    }
                },
                returnType = SourceCodeConstants.void
        )
        return Pair(m1, m2)
    }

    private fun paramsSize(size1: Int, size2: Int): Pair<Method, Method> {
        val a = 97
        val params1 = (0 until size1).map { (a + it).toChar().toString() }
        val params2 = (0 until size2).map { (a + it).toChar().toString() }

        val m1 = PlainMethod(
                clazz = "aaa",
                name = "aaa",
                params = params1,
                returnType = SourceCodeConstants.void
        )
        val m2 = PlainMethod(
                clazz = "aaa",
                name = "aaa",
                params = params2,
                returnType = SourceCodeConstants.void
        )
        return Pair(m1, m2)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 5, 10])
    fun plainMethodsUnequalParamsType(pos: Int) {
        val (m1, m2) = paramsUnequalType(pos, 10)

        val c1 = MethodComparator.compare(m1, m2)
        Assertions.assertTrue(c1 < 0, "$m1 !< $m2")

        val c2 = MethodComparator.compare(m2, m1)
        Assertions.assertTrue(c2 > 0, "$m2 !> $m1")
    }

    @Test
    fun plainMethodsUnequalParamsSize() {
        val (m1, m2) = paramsSize(8, 10)

        val c1 = MethodComparator.compare(m1, m2)
        Assertions.assertTrue(c1 < 0, "$m1 !< $m2")

        val c2 = MethodComparator.compare(m2, m1)
        Assertions.assertTrue(c2 > 0, "$m2 !> $m1")
    }

    private fun jmhParamsUnequalType(pos: Int, size: Int, changeName: Boolean): Pair<Benchmark, Benchmark> {
        val a = 97
        val params = (0 until size).map { (a + it).toChar().toString() }
        val jmhParameters = (0 until size).map { Pair((a + it).toChar().toString(), "$it") }
        val m1 = Benchmark(
                clazz = "aaa",
                name = "aaa",
                params = params,
                jmhParams = jmhParameters,
                returnType = SourceCodeConstants.void
        )
        val m2 = Benchmark(
                clazz = "aaa",
                name = "aaa",
                params = params,
                jmhParams = jmhParameters.mapIndexed { i, v ->
                    if (i + 1 == pos) {
                        if (changeName) {
                            Pair("z", v.second)
                        } else {
                            Pair(v.first, "${pos + 1}")
                        }
                    } else {
                        v
                    }
                },
                returnType = SourceCodeConstants.void
        )
        return Pair(m1, m2)
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 5, 10])
    fun benchsUnequalJmhParamNames(pos: Int) {
        val (m1, m2) = jmhParamsUnequalType(pos, 10, true)

        val c1 = MethodComparator.compare(m1, m2)
        Assertions.assertTrue(c1 < 0, "$m1 !< $m2")

        val c2 = MethodComparator.compare(m2, m1)
        Assertions.assertTrue(c2 > 0, "$m2 !> $m1")
    }

    private fun jmhParamsSize(size1: Int, size2: Int): Pair<Benchmark, Benchmark> {
        val a = 97
        val params1 = (0 until size1).map { (a + it).toChar().toString() }
        val jmhParameters1 = (0 until size1).map { Pair((a + it).toChar().toString(), "$it") }
        val m1 = Benchmark(
                clazz = "aaa",
                name = "aaa",
                params = params1,
                jmhParams = jmhParameters1,
                returnType = SourceCodeConstants.void
        )

        val params2 = (0 until size2).map { (a + it).toChar().toString() }
        val jmhParameters2 = (0 until size2).map { Pair((a + it).toChar().toString(), "$it") }
        val m2 = Benchmark(
                clazz = "aaa",
                name = "aaa",
                params = params2,
                jmhParams = jmhParameters2,
                returnType = SourceCodeConstants.void
        )
        return Pair(m1, m2)
    }

    @Test
    fun benchsUnequalJmhParamSizes() {
        val (m1, m2) = jmhParamsSize(8, 10)

        val c1 = MethodComparator.compare(m1, m2)
        Assertions.assertTrue(c1 < 0, "$m1 !< $m2")

        val c2 = MethodComparator.compare(m2, m1)
        Assertions.assertTrue(c2 > 0, "$m2 !> $m1")
    }
}
