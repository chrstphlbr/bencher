package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.change.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FullChangeSelectorBenchmarkTest : AbstractFullChangeSelectionTest() {

    fun affectedChanges(m: Benchmark, cs: Set<Change>) {
        affectedChanges(m, cs, emptyCg)
        affectedChanges(m, cs, fullCg)
    }

    fun affectedChanges(m: Benchmark, cs: Set<Change>, cgResult: CGResult) {
        val s = FullChangeSelector(cgResult, cs)
        val bs = listOf(m)

        val erbs = s.select(bs)
        val rbs = assertSelection(erbs)
        Assertions.assertTrue(rbs.size == 1)
        Assertions.assertTrue(rbs.contains(m))
    }

    fun notAffectedChanges(m: Benchmark, cs: Set<Change>) {
        notAffectedChanges(m, cs, emptyCg)
        notAffectedChanges(m, cs, fullCg)
    }

    fun notAffectedChanges(m: Benchmark, cs: Set<Change>, cgResult: CGResult) {
        val s = FullChangeSelector(cgResult, cs)
        val bs = listOf(m)

        val erbs = s.select(bs)
        val rbs = assertSelection(erbs)
        Assertions.assertTrue(rbs.isEmpty())
        Assertions.assertFalse(rbs.contains(m))
    }


    @Test
    fun emptyChangeSetCGSet() {
        val s = FullChangeSelector(emptyCg, setOf())

        val eb = s.select(listOf(b1.bench1))
        val b = assertSelection(eb)
        Assertions.assertTrue(b.isEmpty())

        val ebs = s.select(listOf(b1.bench1, b2.bench2, b3.bench3, b4.bench4))
        val bs = assertSelection(ebs)
        Assertions.assertTrue(bs.isEmpty())
    }

    @Test
    fun emptyCGResult() {
        val change = MethodChange(b2.bench2)
        val s = FullChangeSelector(emptyCg, setOf(change))

        val eb = s.select(listOf(b1.bench1))
        val b = assertSelection(eb)
        Assertions.assertTrue(b.isEmpty())

        val ebs = s.select(listOf(b1.bench1, b3.bench3, b4.bench4))
        val bs = assertSelection(ebs)
        Assertions.assertTrue(bs.isEmpty())
    }

    @Test
    fun emptyChangeSet() {
        val s = FullChangeSelector(fullCg, setOf())

        val eb = s.select(listOf(b1.bench1))
        val b = assertSelection(eb)
        Assertions.assertTrue(b.isEmpty())

        val ebs = s.select(listOf(b1.bench1, b4.bench4, b3.bench3, b4.bench4))
        val bs = assertSelection(ebs)
        Assertions.assertTrue(bs.isEmpty())
    }

    @Test
    fun changeBenchBody() {
        affectedChanges(b1.bench1, setOf(MethodChange(b1.bench1)))
    }

    @Test
    fun newBench() {
        affectedChanges(b1.bench1, setOf(AdditionChange(MethodChange(b1.bench1))))

        affectedChanges(
                b1.bench1,
                setOf(AdditionChange(ClassMethodChange(
                        clazz = Class(name = b1.fqn),
                        method = b1.bench1
                )))
        )
    }

    @Test
    fun changeBenchSetup() {
        affectedChanges(b1.bench1, setOf(MethodChange(b1.setup)))
    }

    @Test
    fun addedBenchSetup() {
        affectedChanges(
                b1.bench1,
                setOf(AdditionChange(
                        ClassMethodChange(
                                clazz = Class(name = b1.fqn),
                                method = b1.setup)
                ))
        )
    }

    @Test
    fun removedBenchSetup() {
        affectedChanges(
                b1.bench1,
                setOf(DeletionChange(
                        ClassMethodChange(
                                clazz = Class(name = b1.fqn),
                                method = b1.setup)
                ))
        )
    }


    @Test
    fun changeBenchTearDown() {
        affectedChanges(
                b3.bench3,
                setOf(MethodChange(b3.tearDown))
        )
    }

    @Test
    fun addedBenchTearDown() {
        affectedChanges(
                b3.bench3,
                setOf(AdditionChange(
                        ClassMethodChange(
                                clazz = Class(name = b3.fqn),
                                method = b3.tearDown)
                ))
        )
    }

    @Test
    fun removedTearDown() {
        affectedChanges(
                b3.bench3,
                setOf(DeletionChange(
                        ClassMethodChange(
                                clazz = Class(name = b3.fqn),
                                method = b3.tearDown)
                ))
        )
    }

    @Test
    fun changeBenchInit() {
        // empty arg constructor
        affectedChanges(
                b1.bench1,
                setOf(MethodChange(PlainMethod(clazz = JarTestHelper.BenchParameterized.fqn, name = "<init>", params = listOf())))
        )
        // single argument constructor
        affectedChanges(
                b1.bench1,
                setOf(MethodChange(PlainMethod(clazz = JarTestHelper.BenchParameterized.fqn, name = "<init>", params = listOf("java.lang.String"))))
        )
        // class initializer
        affectedChanges(
                b1.bench1,
                setOf(MethodChange(PlainMethod(clazz = JarTestHelper.BenchParameterized.fqn, name = "<clinit>", params = listOf())))
        )
    }

    @Test
    fun benchNotAffectedByChangeOfOtherMethod() {
        val b = b3.bench3

        val benchChange = setOf(MethodChange(b1.bench1))
        notAffectedChanges(b, benchChange)

        val methodChange = setOf(MethodChange(PlainMethod(
                clazz = b3.fqn,
                name = "otherMethod",
                params = listOf()
        )))

        notAffectedChanges(b, methodChange)
    }

    @Test
    fun benchNotAffectedByAddedOtherMethod() {
        val b = b3.bench3
        val cs = setOf(AdditionChange(
                ClassMethodChange(
                        clazz = Class(name = b3.fqn),
                        method = PlainMethod(
                                clazz = b3.fqn,
                                name = "otherMethod",
                                params = listOf()
                        )
                )
        ))
        notAffectedChanges(b, cs)
    }

    @Test
    fun benchNotAffectedByDeletedOtherMethod() {
        val b = b3.bench3
        val cs = setOf(AdditionChange(
                ClassMethodChange(
                        clazz = Class(name = b3.fqn),
                        method = PlainMethod(
                                clazz = b3.fqn,
                                name = "otherMethod",
                                params = listOf()
                        )
                )
        ))

        notAffectedChanges(b, cs)
    }
}
