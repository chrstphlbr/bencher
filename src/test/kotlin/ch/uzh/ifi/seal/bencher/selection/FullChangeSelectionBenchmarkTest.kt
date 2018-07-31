package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.change.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FullChangeSelectionBenchmarkTest : AbstractFullChangeSelectionTest() {

    fun affectedChanges(m: Benchmark, c: Change) {
        val a = cs.affected(m, c, CGResult(mapOf()))
        Assertions.assertTrue(a)
        val a2 = cs.affected(m, c, fullCg)
        Assertions.assertTrue(a2)

        val a3 = cs.affected(m, listOf(c), CGResult(mapOf()))
        Assertions.assertTrue(a3)
        val a4 = cs.affected(m, listOf(c), fullCg)
        Assertions.assertTrue(a4)

        val bs = cs.affected(listOf(m), listOf(c), CGResult(mapOf()))
        Assertions.assertTrue(bs.contains(m))
        val bs2 = cs.affected(listOf(m), listOf(c), fullCg)
        Assertions.assertTrue(bs2.contains(m))
    }

    fun notAffectedChanges(m: Benchmark, c: Change) {
        val a = cs.affected(m, c, CGResult(mapOf()))
        Assertions.assertFalse(a)
        val a2 = cs.affected(m, c, fullCg)
        Assertions.assertFalse(a2)

        val a3 = cs.affected(m, listOf(c), CGResult(mapOf()))
        Assertions.assertFalse(a3)
        val a4 = cs.affected(m, listOf(c), fullCg)
        Assertions.assertFalse(a4)

        val bs = cs.affected(listOf(m), listOf(c), CGResult(mapOf()))
        Assertions.assertFalse(bs.contains(m))
        val bs2 = cs.affected(listOf(m), listOf(c), fullCg)
        Assertions.assertFalse(bs2.contains(m))
    }


    @Test
    fun emptyChangeSetCGSet() {
        val a = cs.affected(b1.bench1, listOf(), CGResult(mapOf()))
        Assertions.assertFalse(a)
        val bs = cs.affected(listOf(b1.bench1, b2.bench2, b3.bench3, b4.bench4), listOf(), CGResult(mapOf()))
        Assertions.assertTrue(bs.toList().isEmpty())
    }

    @Test
    fun emptyCGResult() {
        val change = MethodChange(b2.bench2)
        val a = cs.affected(b1.bench1, change, CGResult(mapOf()))
        Assertions.assertFalse(a)
        val a2 = cs.affected(b1.bench1, listOf(change), CGResult(mapOf()))
        Assertions.assertFalse(a2)
        val bbs = cs.affected(listOf(b1.bench1, b3.bench3, b4.bench4), listOf(change), CGResult(mapOf()))
        Assertions.assertTrue(bbs.toList().isEmpty())
    }

    @Test
    fun emptyChangeSet() {
        val change = MethodChange(b2.bench2)
        val a = cs.affected(b1.bench1, change, fullCg)
        Assertions.assertFalse(a)
        val a2 = cs.affected(b1.bench1, listOf(), fullCg)
        Assertions.assertFalse(a2)
        val bbs = cs.affected(listOf(b1.bench1, b4.bench4, b3.bench3, b4.bench4), listOf(), fullCg)
        Assertions.assertTrue(bbs.toList().isEmpty())
    }

    @Test
    fun changeBenchBody() {
        affectedChanges(b1.bench1, MethodChange(b1.bench1))
    }

    @Test
    fun newBench() {
        affectedChanges(b1.bench1, AdditionChange(MethodChange(b1.bench1)))
        affectedChanges(b1.bench1, AdditionChange(ClassMethodChange(
                clazz = Class(file = "", name = b1.fqn),
                method = b1.bench1
        )))
    }

    @Test
    fun changeBenchSetup() {
        affectedChanges(b1.bench1, MethodChange(b1.setup))
    }

    @Test
    fun addedBenchSetup() {
        affectedChanges(b1.bench1, AdditionChange(
                ClassMethodChange(
                        clazz = Class("", b1.fqn),
                        method = b1.setup)
        ))
    }

    @Test
    fun removedBenchSetup() {
        affectedChanges(b1.bench1, DeletionChange(
                ClassMethodChange(
                        clazz = Class("", b1.fqn),
                        method = b1.setup)
        ))
    }


    @Test
    fun changeBenchTearDown() {
        affectedChanges(b3.bench3, MethodChange(b3.tearDown))
    }

    @Test
    fun addedBenchTearDown() {
        affectedChanges(b3.bench3, AdditionChange(
                ClassMethodChange(
                        clazz = Class("", b3.fqn),
                        method = b3.tearDown)
        ))
    }

    @Test
    fun removedTearDown() {
        affectedChanges(b3.bench3, DeletionChange(
                ClassMethodChange(
                        clazz = Class("", b3.fqn),
                        method = b3.tearDown)
        ))
    }

    @Test
    fun changeBenchInit() {
        // empty arg constructor
        affectedChanges(b1.bench1, MethodChange(PlainMethod(clazz = JarTestHelper.BenchParameterized.fqn, name = "<init>", params = listOf())))
        // single argument constructor
        affectedChanges(b1.bench1, MethodChange(PlainMethod(clazz = JarTestHelper.BenchParameterized.fqn, name = "<init>", params = listOf("java.lang.String"))))
        // class initializer
        affectedChanges(b1.bench1, MethodChange(PlainMethod(clazz = JarTestHelper.BenchParameterized.fqn, name = "<clinit>", params = listOf())))
    }

    @Test
    fun benchNotAffectedByChangeOfOtherMethod() {
        notAffectedChanges(b3.bench3, MethodChange(b1.bench1))
        notAffectedChanges(b3.bench3, MethodChange(PlainMethod(
                clazz = b3.fqn,
                name = "otherMethod",
                params = listOf()
        )))
    }

    @Test
    fun benchNotAffectedByAddedOtherMethod() {
        notAffectedChanges(b3.bench3, AdditionChange(
                ClassMethodChange(
                        clazz = Class("", b3.fqn),
                        method = PlainMethod(
                                clazz = b3.fqn,
                                name = "otherMethod",
                                params = listOf()
                        ))
        ))
    }

    @Test
    fun benchNotAffectedByDeletedOtherMethod() {
        notAffectedChanges(b3.bench3, AdditionChange(
                ClassMethodChange(
                        clazz = Class("", b3.fqn),
                        method = PlainMethod(
                                clazz = b3.fqn,
                                name = "otherMethod",
                                params = listOf()
                        ))
        ))
    }
}
