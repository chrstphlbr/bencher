package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper
import ch.uzh.ifi.seal.bencher.analysis.change.MethodChange
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ChangeAwarePrioritizerTest {

    @Test
    fun noChange() {
        val cg = PrioritizerTestHelper.cgFull
        val mws = PrioritizerTestHelper.mwFull
        val benchs = PrioritizerTestHelper.benchs

        val p = AdditionalPrioritizer(cg, mws)

        val cp1 = SelectionAwarePrioritizer(
                selector = FullChangeSelector(cg, setOf()),
                prioritizer = p,
                singlePrioritization = false
        )

        val cp2 = SelectionAwarePrioritizer(
                selector = FullChangeSelector(cg, setOf()),
                prioritizer = p,
                singlePrioritization = true
        )

        // same as non-change-aware prioritization, i.e., the prioritizer that was passed in

        val epbs = p.prioritize(benchs)
        if (epbs.isLeft()) {
            Assertions.fail<String>("Could not prioritze with p: ${epbs.left().get()}")
        }
        val pbs = epbs.right().get()

        val ecp1bs = cp1.prioritize(benchs)
        if (ecp1bs.isLeft()) {
            Assertions.fail<String>("Could not prioritze with cp1: ${ecp1bs.left().get()}")
        }
        val cp1bs = ecp1bs.right().get()

        val ecp2bs = cp2.prioritize(benchs)
        if (ecp2bs.isLeft()) {
            Assertions.fail<String>("Could not prioritze with cp2: ${ecp2bs.left().get()}")
        }
        val cp2bs = ecp2bs.right().get()

        Assertions.assertTrue(pbs == cp1bs)
        Assertions.assertTrue(cp1bs == cp2bs)
        Assertions.assertTrue(pbs == cp2bs)
    }

    @Test
    fun withChangeChangeSetPrio() {
        val cg = PrioritizerTestHelper.cgFull
        val mws = PrioritizerTestHelper.mwFull
        val benchs = PrioritizerTestHelper.benchs
        val changes = setOf(MethodChange(method = JarTestHelper.CoreB.m))

        val p = SelectionAwarePrioritizer(
                selector = FullChangeSelector(cg, changes),
                prioritizer = AdditionalPrioritizer(cg, mws),
                singlePrioritization = false
        )

        val epbs = p.prioritize(benchs)
        if (epbs.isLeft()) {
            Assertions.fail<String>("Could not prioritize benchmarks: ${epbs.left().get()}")
        }
        val pbs = epbs.right().get()

        Assertions.assertTrue(pbs.size == 4)

        val b1 = pbs[0]
        PrioritizerTestHelper.assertBenchmark(b1, JarTestHelper.BenchParameterized.bench1, 1, 4, 5.75)

        val b2 = pbs[1]
        PrioritizerTestHelper.assertBenchmark(b2, JarTestHelper.OtherBench.bench3, 2, 4, 0.0)

        val b3 = pbs[2]
        PrioritizerTestHelper.assertBenchmark(b3, JarTestHelper.BenchNonParameterized.bench2, 3, 4, 3.0)

        val b4 = pbs[3]
        PrioritizerTestHelper.assertBenchmark(b4, JarTestHelper.BenchParameterized2.bench4, 4, 4, 2.5)
    }

    @Test
    fun withChangeSinglePrio() {
        val cg = PrioritizerTestHelper.cgFull
        val mws = PrioritizerTestHelper.mwFull
        val benchs = PrioritizerTestHelper.benchs
        val changes = setOf(MethodChange(method = JarTestHelper.CoreB.m))

        val p = SelectionAwarePrioritizer(
                selector = FullChangeSelector(cg, changes),
                prioritizer = AdditionalPrioritizer(cg, mws),
                singlePrioritization = true
        )

        val epbs = p.prioritize(benchs)
        if (epbs.isLeft()) {
            Assertions.fail<String>("Could not prioritize benchmarks: ${epbs.left().get()}")
        }
        val pbs = epbs.right().get()



        Assertions.assertTrue(pbs.size == 4)

        val b1 = pbs[0]
        PrioritizerTestHelper.assertBenchmark(b1, JarTestHelper.BenchParameterized.bench1, 1, 4, 5.75)

        val b2 = pbs[1]
        PrioritizerTestHelper.assertBenchmark(b2, JarTestHelper.OtherBench.bench3, 2, 4, 0.0)

        val b3 = pbs[2]
        PrioritizerTestHelper.assertBenchmark(b3, JarTestHelper.BenchParameterized2.bench4, 3, 4, 2.0)

        val b4 = pbs[3]
        PrioritizerTestHelper.assertBenchmark(b4, JarTestHelper.BenchNonParameterized.bench2, 4, 4, 0.0)
    }
}
