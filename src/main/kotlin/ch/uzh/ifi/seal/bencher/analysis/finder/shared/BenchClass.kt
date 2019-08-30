package ch.uzh.ifi.seal.bencher.analysis.finder.shared

import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration
import org.funktionale.option.Option

class BenchClass(private val som: StateObjectManager? = null) {
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

    private val jmhParams = mutableMapOf<String, MutableList<String>>()
    val methodHashes = mutableMapOf<Method, ByteArray>()

    fun setClassExecInfo() {
        classExecConfig = if (benchs.isEmpty()) {
            Option.empty()
        } else {
            ExecutionConfigurationHelper.toExecutionConfiguration(forkVisitor, measurementVisitor, warmupVisitor, benchModeVisitor, outputTimeUnitAnnotationVisitor)
        }
    }

    fun setJmhParams(bfs: List<BenchField>) {
        bfs.filter { it.isParam }.forEach { fv ->
            fv.jmhParams.forEach { (name, values) ->
                jmhParams[name] = values
            }
        }
    }

    fun setBenchs(className: String, bms: List<BenchMethod>) {
        bms.forEach { m ->
            val method: Method
            if (m.isBench) {
                method = MF.benchmark(
                        clazz = className,
                        name = m.name,
                        params = m.params,
                        returnType = m.returnType,
                        jmhParams = jmhParamsToList(som?.getBenchmarkJmhParams(jmhParams, m.params) ?: jmhParams),
                        group = m.group()
                )

                benchs.add(method)

                val execInfo = m.execConfig
                if (execInfo.isDefined()) {
                    benchExecInfos[method] = execInfo.get()
                }
            } else if (m.isSetup) {
                method = MF.setupMethod(
                        clazz = className,
                        name = m.name,
                        params = m.params,
                        returnType = m.returnType
                )

                setups.add(method)
            } else if (m.isTearDown) {
                method = MF.tearDownMethod(
                        clazz = className,
                        name = m.name,
                        params = m.params,
                        returnType = m.returnType
                )

                tearDowns.add(method)
            } else {
                method = MF.plainMethod(
                        clazz = className,
                        name = m.name,
                        params = listOf()
                )
            }

            val hash = m.hash
            if (hash != null) {
                methodHashes[method] = hash
            }
        }
    }

    private fun jmhParamsToList(input: MutableMap<String, MutableList<String>>): JmhParameters {
        return input.flatMap { (name, values) ->
            values.map { value ->
                Pair(name, value)
            }
        }
    }
}