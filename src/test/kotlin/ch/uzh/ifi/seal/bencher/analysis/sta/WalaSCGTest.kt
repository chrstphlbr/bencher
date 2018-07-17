package ch.uzh.ifi.seal.bencher.analysis.sta

import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


abstract class WalaSCGTest {

    abstract val cg: CGResult

    @Test
    fun allBenchs() {
        Assertions.assertTrue(cg.benchCalls.containsKey(h.bench1), "bench1 not present")
        Assertions.assertTrue(cg.benchCalls.containsKey(h.bench2), "bench2 not present")
        Assertions.assertTrue(cg.benchCalls.containsKey(h.bench3), "bench3 not present")
    }

    @Test
    fun libCallsBench1() {
        val calls = cg.benchCalls.get(h.bench1)
        if (calls == null) {
            Assertions.fail<String>("bench1 not present")
            return
        }

        val a1 = calls.contains(h.possibleMethodCall(h.coreA, 1, 2, 0))
        Assertions.assertTrue(a1, h.errStr("A.m", 1))

        val b1 = calls.contains(h.possibleMethodCall(h.coreB, 1, 2, 0))
        Assertions.assertTrue(b1, h.errStr("B.m", 1))

        val a2 = calls.contains(h.possibleMethodCall(h.coreA, 2, 2, 5))
        Assertions.assertTrue(a2, h.errStr("A.m", 2))
        val b2 = calls.contains(h.possibleMethodCall(h.coreB, 2, 2, 5))
        Assertions.assertTrue(b2, h.errStr("B.m", 2))
        val c2 = calls.contains(h.plainMethodCall(h.coreC, 2))
        Assertions.assertTrue(c2, h.errStr("C.m", 2))
    }

    @Test
    fun libCallsBench2() {
        val calls = cg.benchCalls.get(h.bench2)
        if (calls == null) {
            Assertions.fail<String>("bench2 not present")
            return
        }

        val c1 = calls.contains(h.plainMethodCall(h.coreC, 1))
        Assertions.assertTrue(c1, h.errStr("C.m", 1))
    }

    @Test
    fun libCallsBench3() {
        val calls = cg.benchCalls.get(h.bench3)
        if (calls == null) {
            Assertions.fail<String>("bench3 not present")
            return
        }

        val b1 = calls.contains(h.plainMethodCall(h.coreB, 1))
        Assertions.assertTrue(b1, h.errStr("B.m", 1))
    }

    companion object {
        val h = WalaSCGTestHelper
    }
}
