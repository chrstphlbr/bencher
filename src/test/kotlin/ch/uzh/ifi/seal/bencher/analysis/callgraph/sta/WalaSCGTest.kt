package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


abstract class WalaSCGTest {

    abstract val cgr: CGResult

    @Test
    fun allBenchs() {
        Assertions.assertTrue(cgr.calls.containsKey(bench1), "bench1 not present")
        Assertions.assertEquals(bench1, cgr.calls.getValue(bench1).start, "bench1 not start node of CG")
        Assertions.assertTrue(cgr.calls.containsKey(bench2), "bench2 not present")
        Assertions.assertEquals(bench2, cgr.calls.getValue(bench2).start, "bench2 not start node of CG")
        Assertions.assertTrue(cgr.calls.containsKey(bench3), "bench3 not present")
        Assertions.assertEquals(bench3, cgr.calls.getValue(bench3).start, "bench3 not start node of CG")
        Assertions.assertTrue(cgr.calls.containsKey(bench4), "bench4 not present")
        Assertions.assertEquals(bench4, cgr.calls.getValue(bench4).start, "bench4 not start node of CG")
    }

    @Test
    fun libCallsBench1() {
        val cg = cgr.calls[bench1]
        if (cg == null) {
            Assertions.fail<String>("bench1 not present")
            return
        }

        if (multiCGEntrypoints) {
            // calls in benchmark
//            val a1 = cg.contains(MethodCall(bench1, coreA, nrPossibleTargets = 2, idPossibleTargets = 0))
//            Assertions.assertTrue(a1, h.errStr("A.m", 1))
            h.reachable(cg, bench1, coreA, 1, true, 0.5)
//            val b1 = cg.contains(MethodCall(bench1, coreB, nrPossibleTargets = 2, idPossibleTargets = 0))
//            Assertions.assertTrue(b1, h.errStr("B.m", 1))
            h.reachable(cg, bench1, coreB, 1, true, 0.5)

            // calls in A.m
//            val a2 = cg.contains(MethodCall(coreA, coreA, nrPossibleTargets = 2, idPossibleTargets = 5))
//            Assertions.assertTrue(a2, h.errStr("A.m", 2))
//            val b2 = cg.contains(MethodCall(coreA, coreB, nrPossibleTargets = 2, idPossibleTargets = 5))
//            Assertions.assertTrue(b2, h.errStr("B.m", 2))

            // calls in B.m
//            val c2 = cg.contains(MethodCall(coreB, coreC, nrPossibleTargets = 1, idPossibleTargets = 5))
//            Assertions.assertTrue(c2, h.errStr("C.m", 2))
            h.reachable(cg, bench1, coreC, 2, true, 0.5)
        } else {
            // calls in benchmark
//            val a1 = cg.contains(MethodCall(bench1, coreA, nrPossibleTargets = 3, idPossibleTargets = 0))
//            Assertions.assertTrue(a1, h.errStr("A.m", 1))
            h.reachable(cg, bench1, coreA, 1, true, 0.33)
//            val b1 = cg.contains(MethodCall(bench1, coreB, nrPossibleTargets = 3, idPossibleTargets = 0))
//            Assertions.assertTrue(b1, h.errStr("B.m", 1))
            h.reachable(cg, bench1, coreB, 1, true, 0.33)
//            val d1 = cg.contains(MethodCall(bench1, coreD, nrPossibleTargets = 3, idPossibleTargets = 0))
//            Assertions.assertTrue(d1, h.errStr("D.m", 1))
            h.reachable(cg, bench1, coreD, 1, true, 0.33)

            // calls in A.m
//            val a2 = cg.contains(MethodCall(coreA, coreA, nrPossibleTargets = 3, idPossibleTargets = 5))
//            Assertions.assertTrue(a2, h.errStr("A.m", 2))
//            val b2 = cg.contains(MethodCall(coreA, coreB, nrPossibleTargets = 3, idPossibleTargets = 5))
//            Assertions.assertTrue(b2, h.errStr("B.m", 2))
//            val d2 = cg.contains(MethodCall(coreA, coreD, nrPossibleTargets = 3, idPossibleTargets = 5))
//            Assertions.assertTrue(d2, h.errStr("D.m", 2))

            // calls in B.m
//            val c2 = cg.contains(MethodCall(coreB, coreC, nrPossibleTargets = 1, idPossibleTargets = 5))
//            Assertions.assertTrue(c2, h.errStr("C.m", 2))
            h.reachable(cg, bench1, coreC, 2, true, 0.33)

            // calls in D.m
            // no calls to other libary methods
        }
    }

    @Test
    fun libCallsBench2() {
        val cg = cgr.calls[bench2]
        if (cg == null) {
            Assertions.fail<String>("bench2 not present")
            return
        }

        // calls in bench
//        val c1 = cg.contains(MethodCall(bench2, coreC, nrPossibleTargets = 1, idPossibleTargets = 0))
//        Assertions.assertTrue(c1, h.errStr("C.m", 1))
        h.reachable(cg, bench2, coreC, 1, false, 1.0)
    }

    @Test
    fun libCallsBench3() {
        val cg = cgr.calls[bench3]
        if (cg == null) {
            Assertions.fail<String>("bench3 not present")
            return
        }

        // calls in bench
//        val b1 = cg.contains(MethodCall(bench3, coreB, nrPossibleTargets = 1, idPossibleTargets = 0))
//        Assertions.assertTrue(b1, h.errStr("B.m", 1))
        h.reachable(cg, bench3, coreB, 1, false, 1.0)

        // calls in B.m
//        val c2 = cg.contains(MethodCall(coreB, coreC, nrPossibleTargets = 1, idPossibleTargets = 5))
//        Assertions.assertTrue(c2, h.errStr("C.m", 2))
        h.reachable(cg, bench3, coreC, 2, false, 1.0)
    }

    @Test
    fun libCallsBench4() {
        val b = bench4
        val cg = cgr.calls[b]
        if (cg == null) {
            Assertions.fail<String>("bench4 not present")
            return
        }

        if (multiCGEntrypoints) {
            // calls in bench
//            val a1 = cg.contains(MethodCall(b, coreA, nrPossibleTargets = 2, idPossibleTargets = 0))
//            Assertions.assertTrue(a1, h.errStr("A.m", 1))
            h.reachable(cg, b, coreA, 1, true, 0.5)
//            val d1 = cg.contains(MethodCall(b, coreD, nrPossibleTargets = 2, idPossibleTargets = 0))
//            Assertions.assertTrue(d1, h.errStr("D.m", 1))
            h.reachable(cg, b, coreD, 1, true, 0.5)

            // calls in A.m
//            val a2 = cg.contains(MethodCall(coreA, coreA, nrPossibleTargets = 2, idPossibleTargets = 5))
//            Assertions.assertTrue(a2, h.errStr("A.m", 2))
//            val d2 = cg.contains(MethodCall(coreA, coreD, nrPossibleTargets = 2, idPossibleTargets = 5))
//            Assertions.assertTrue(d2, h.errStr("D.m", 2))

            // calls in D.m
            // no calls to other libary methods
        } else {
            // calls in benchmark
//            val a1 = cg.contains(MethodCall(b, coreA, nrPossibleTargets = 3, idPossibleTargets = 0))
//            Assertions.assertTrue(a1, h.errStr("A.m", 1))
            h.reachable(cg, b, coreA, 1, true, 0.33)
//            val b1 = cg.contains(MethodCall(b, coreB, nrPossibleTargets = 3, idPossibleTargets = 0))
//            Assertions.assertTrue(b1, h.errStr("B.m", 1))
            h.reachable(cg, b, coreB, 1, true, 0.33)
//            val d1 = cg.contains(MethodCall(b, coreD, nrPossibleTargets = 3, idPossibleTargets = 0))
//            Assertions.assertTrue(d1, h.errStr("D.m", 1))
            h.reachable(cg, b, coreD, 1, true, 0.33)

            // calls in A.m
//            val a2 = cg.contains(MethodCall(coreA, coreA, nrPossibleTargets = 3, idPossibleTargets = 5))
//            Assertions.assertTrue(a2, h.errStr("A.m", 2))
//            val b2 = cg.contains(MethodCall(coreA, coreB, nrPossibleTargets = 3, idPossibleTargets = 5))
//            Assertions.assertTrue(b2, h.errStr("B.m", 2))
//            val d2 = cg.contains(MethodCall(coreA, coreD, nrPossibleTargets = 3, idPossibleTargets = 5))
//            Assertions.assertTrue(d2, h.errStr("D.m", 2))

            // calls in B.m
//            val c2 = cg.contains(MethodCall(coreB, coreC, nrPossibleTargets = 1, idPossibleTargets = 5))
//            Assertions.assertTrue(c2, h.errStr("C.m", 2))
            h.reachable(cg, b, coreC, 2, true, 0.33)

            // calls in D.m
            // no calls to other libary methods
        }
    }

    abstract val multiCGEntrypoints: Boolean

    companion object {
        val h = WalaSCGTestHelper
        val bench1 = JarTestHelper.BenchParameterized.bench1
        val bench2 = JarTestHelper.BenchNonParameterized.bench2
        val bench3 = JarTestHelper.OtherBench.bench3
        val bench4 = JarTestHelper.BenchParameterized2.bench4

        val coreA = JarTestHelper.CoreA.m
        val coreB = JarTestHelper.CoreB.m
        val coreC = JarTestHelper.CoreC.m
        val coreD = JarTestHelper.CoreD.m
    }
}
