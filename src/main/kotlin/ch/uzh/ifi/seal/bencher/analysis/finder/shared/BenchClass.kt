package ch.uzh.ifi.seal.bencher.analysis.finder.shared

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.MF
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration
import org.funktionale.option.Option

class BenchClass {
    val benchs: MutableSet<Benchmark> = mutableSetOf()
    val setups: MutableSet<SetupMethod> = mutableSetOf()
    val tearDowns: MutableSet<TearDownMethod> = mutableSetOf()
    lateinit var classExecConfig: Option<ExecutionConfiguration>
        private set
    val benchExecInfos: MutableMap<Benchmark, ExecutionConfiguration> = mutableMapOf()

    // sub visitor
    var forkVisitor: BenchForkAnnotation? = null
    var measurementVisitor: BenchIterationAnnotation? = null
    var warmupVisitor: BenchIterationAnnotation? = null
    var benchModeVisitor: BenchModeAnnotation? = null
    var outputTimeUnitAnnotationVisitor: BenchOutputTimeUnitAnnotation? = null

    private lateinit var jmhParams: List<Pair<String, String>>

    fun setClassExecInfo() {
        classExecConfig = if (benchs.isEmpty()) {
            Option.empty()
        } else {
            ExecutionConfigurationHelper.toExecutionConfiguration(forkVisitor, measurementVisitor, warmupVisitor, benchModeVisitor, outputTimeUnitAnnotationVisitor)
        }
    }

    fun setJmhParams(bfs: List<BenchField>) {
        jmhParams = bfs.filter { it.isParam }.flatMap { fv ->
            fv.jmhParams.flatMap { (name, values) ->
                values.map { value ->
                    Pair(name, value)
                }
            }
        }
    }

    fun setBenchs(className: String, bms: List<BenchMethod>) {
        bms.forEach { m ->
            if (m.isBench) {
                val bench = MF.benchmark(
                        clazz = className,
                        name = m.name,
                        params = m.params,
                        jmhParams = jmhParams
                )

                benchs.add(bench)

                val execInfo = m.execConfig
                if (execInfo.isDefined()) {
                    benchExecInfos[bench] = execInfo.get()
                }
            } else if (m.isSetup) {
                val setup = MF.setupMethod(
                        clazz = className,
                        name = m.name,
                        params = m.params
                )

                setups.add(setup)
            } else if (m.isTearDown) {
                val tearDown = MF.tearDownMethod(
                        clazz = className,
                        name = m.name,
                        params = m.params
                )

                tearDowns.add(tearDown)
            }
        }
    }
}