package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MethodCallComparatorTest {

    private fun from(): PlainMethod =
            PlainMethod(
                    clazz = "aaa",
                    name = "aaa",
                    params = listOf("aaa", "bbb", "ccc"),
                    returnType = SourceCodeConstants.void
            )

    private fun to(): PlainMethod =
            PlainMethod(
                    clazz = "bbb",
                    name = "bbb",
                    params = listOf("aaa", "bbb", "ccc"),
                    returnType = SourceCodeConstants.void
            )

    private fun methodCall(): MethodCall =
            MethodCall(
                    from = from(),
                    to = to(),
                    idPossibleTargets = 0,
                    nrPossibleTargets = 1
            )

    @Test
    fun equal() {
        val mc1 = methodCall()
        val mc2 = methodCall()
        val c1 = MethodCallComparator.compare(mc1, mc2)
        Assertions.assertEquals(0, c1, "mc1 != mc2")
        val c2 = MethodCallComparator.compare(mc2, mc1)
        Assertions.assertEquals(0, c2, "mc2 != mc1")
    }

    @Test
    fun fromUnequal() {
        val mc1 = methodCall().copy(from = from().copy(clazz = "ccc"))
        val mc2 = methodCall()
        val c1 = MethodCallComparator.compare(mc1, mc2)
        Assertions.assertTrue(c1 > 0, "mc1 !> mc2")
        val c2 = MethodCallComparator.compare(mc2, mc1)
        Assertions.assertTrue(c2 < 0, "mc2 !< mc1")
    }

    @Test
    fun toUnequal() {
        val mc1 = methodCall().copy(to = to().copy(clazz = "ccc"))
        val mc2 = methodCall()
        val c1 = MethodCallComparator.compare(mc1, mc2)
        Assertions.assertTrue(c1 > 0, "mc1 !> mc2")
        val c2 = MethodCallComparator.compare(mc2, mc1)
        Assertions.assertTrue(c2 < 0, "mc2 !< mc1")
    }

    @Test
    fun idUnequal() {
        val mc1 = methodCall().copy(idPossibleTargets = 1)
        val mc2 = methodCall()
        val c1 = MethodCallComparator.compare(mc1, mc2)
        Assertions.assertTrue(c1 > 0, "mc1 !> mc2")
        val c2 = MethodCallComparator.compare(mc2, mc1)
        Assertions.assertTrue(c2 < 0, "mc2 !< mc1")
    }

    @Test
    fun nrUnequal() {
        val mc1 = methodCall().copy(nrPossibleTargets = 100)
        val mc2 = methodCall()
        val c1 = MethodCallComparator.compare(mc1, mc2)
        Assertions.assertTrue(c1 > 0, "mc1 !> mc2")
        val c2 = MethodCallComparator.compare(mc2, mc1)
        Assertions.assertTrue(c2 < 0, "mc2 !< mc1")
    }
}
