package ch.uzh.ifi.seal.bencher.analysis.finder.shared

import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration
import org.funktionale.option.Option

class BenchMethod {
    var isBench: Boolean = false
    var isSetup: Boolean = false
    var isTearDown: Boolean = false
    lateinit var execConfig: Option<ExecutionConfiguration>
        private set

    // sub-visitor
    var forkVisitor: BenchForkAnnotation? = null
    var measurementVisitor: BenchIterationAnnotation? = null
    var warmupVisitor: BenchIterationAnnotation? = null
    var benchModeVisitor: BenchModeAnnotation? = null
    var outputTimeUnitAnnotationVisitor: BenchOutputTimeUnitAnnotation? = null

    fun setExecInfo() {
        val (f, wf) = if (forkVisitor != null) {
            Pair(forkVisitor!!.forks(), forkVisitor!!.warmups())
        } else {
            Pair(-1, -1)
        }

        val (wi, wt, wtu) = if (warmupVisitor != null) {
            Triple(warmupVisitor!!.iterations(), warmupVisitor!!.time(), warmupVisitor!!.timeUnit())
        } else {
            Triple(-1, -1, Option.empty())
        }

        val (mi, mt, mtu) = if (measurementVisitor != null) {
            Triple(measurementVisitor!!.iterations(), measurementVisitor!!.time(), measurementVisitor!!.timeUnit())
        } else {
            Triple(-1, -1, Option.empty())
        }

        val bm = if (benchModeVisitor != null) {
            benchModeVisitor!!.mode()
        } else {
            listOf()
        }

        val otu = if (outputTimeUnitAnnotationVisitor != null) {
            outputTimeUnitAnnotationVisitor!!.timeUnit()
        } else {
            Option.empty()
        }

        execConfig = if (isBench) {
            Option.Some(ExecutionConfiguration(
                    forks = f,
                    warmupForks = wf,
                    measurementIterations = mi,
                    measurementTime = mt,
                    measurementTimeUnit = mtu,
                    warmupIterations = wi,
                    warmupTime = wt,
                    warmupTimeUnit = wtu,
                    mode = bm,
                    outputTimeUnit = otu
            ))
        } else {
            Option.empty()
        }
    }
}