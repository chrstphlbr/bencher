package ch.uzh.ifi.seal.bencher.analysis.finder.shared

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration
import ch.uzh.ifi.seal.bencher.execution.unsetExecConfig

class BenchMethod() {
    lateinit var name: String
    lateinit var params: List<String>
    lateinit var returnType: String
    var hash: ByteArray? = null
    var numberOfLines: Int? = null

    var isBench: Boolean = false
    var isSetup: Boolean = false
    var isTearDown: Boolean = false
    lateinit var execConfig: Option<ExecutionConfiguration>
        private set

    fun group() = groupVisitor?.name

    // sub-visitor
    var groupVisitor: BenchGroupAnnotation? = null
    var forkVisitor: BenchForkAnnotation? = null
    var measurementVisitor: BenchIterationAnnotation? = null
    var warmupVisitor: BenchIterationAnnotation? = null
    var benchModeVisitor: BenchModeAnnotation? = null
    var outputTimeUnitAnnotationVisitor: BenchOutputTimeUnitAnnotation? = null

    constructor(name: String) : this() {
        this.name = name
    }

    fun setExecInfo() {
        execConfig = if (isBench) {
            if (!group().isNullOrEmpty()) {
                val bm = benchModeVisitor?.mode() ?: listOf()

                val config = if (bm.isEmpty()) {
                    unsetExecConfig
                } else {
                    unsetExecConfig.copy(mode = bm)
                }

                Some(config)
            } else {
                ExecutionConfigurationHelper.toExecutionConfiguration(forkVisitor, measurementVisitor, warmupVisitor, benchModeVisitor, outputTimeUnitAnnotationVisitor)
            }
        } else {
            None
        }
    }
}