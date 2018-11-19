package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import ch.uzh.ifi.seal.bencher.analysis.JMHConstants
import ch.uzh.ifi.seal.bencher.analysis.descriptorToParamList
import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration
import org.funktionale.option.Option
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor

class AsmBenchClassVisitor(api: Int, cv: ClassVisitor?, private val className: String) : ClassVisitor(api, cv) {
    private val benchs: MutableSet<Benchmark> = mutableSetOf()
    private val setups: MutableSet<SetupMethod> = mutableSetOf()
    private val tearDowns: MutableSet<TearDownMethod> = mutableSetOf()
    private lateinit var classExecConfig: Option<ExecutionConfiguration>
    private val benchExecInfos: MutableMap<Benchmark, ExecutionConfiguration> = mutableMapOf()

    // sub visitor
    private val mvs: MutableList<AsmBenchMethodVisitor> = mutableListOf()
    private val fvs: MutableList<AsmBenchFieldVisitor> = mutableListOf()
    private var forkVisitor: AsmBenchForkAnnotationVisitor? = null
    private var measurementVisitor: AsmBenchIterationAnnotationVisitor? = null
    private var warmupVisitor: AsmBenchIterationAnnotationVisitor? = null
    private var benchModeVisitor: AsmBenchModeAnnotationVisitor? = null
    private var outputTimeUnitAnnotationVisitor: AsmBenchOutputTimeUnitAnnotationVisitor? = null

    fun benchs(): Set<Benchmark> = benchs
    fun setups(): Set<SetupMethod> = setups
    fun tearDowns(): Set<TearDownMethod> = tearDowns
    // returns Some iff benchs.size > 0
    fun classExecInfo(): Option<ExecutionConfiguration> = classExecConfig
    fun benchExecInfos(): Map<Benchmark, ExecutionConfiguration> = benchExecInfos

    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<String>?): MethodVisitor? {
        val mv = cv?.visitMethod(access, name, descriptor, signature, exceptions)

        val jmhMv = AsmBenchMethodVisitor(
                api = api,
                mv = mv,
                name = name,
                descriptor = descriptor
        )
        mvs.add(jmhMv)
        return jmhMv
    }

    override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
        val fv = AsmBenchFieldVisitor(
                api = api,
                fv = cv?.visitField(access, name, descriptor, signature, value),
                name = name
        )

        fvs.add(fv)

        return fv
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        val sv = cv?.visitAnnotation(descriptor, visible)
        return when (descriptor) {
            JMHConstants.Annotation.fork -> {
                val fv = AsmBenchForkAnnotationVisitor(api, sv)
                forkVisitor = fv
                fv
            }
            JMHConstants.Annotation.measurement -> {
                val mv = AsmBenchIterationAnnotationVisitor(api, sv)
                measurementVisitor = mv
                mv
            }
            JMHConstants.Annotation.warmup -> {
                val wv = AsmBenchIterationAnnotationVisitor(api, sv)
                warmupVisitor = wv
                wv
            }
            JMHConstants.Annotation.mode -> {
                val bmv = AsmBenchModeAnnotationVisitor(api, sv)
                benchModeVisitor = bmv
                bmv
            }
            JMHConstants.Annotation.outputTimeUnit -> {
                val otuv = AsmBenchOutputTimeUnitAnnotationVisitor(api, sv)
                outputTimeUnitAnnotationVisitor = otuv
                otuv
            }
            else -> sv
        }
    }

    override fun visitEnd() {
        cv?.visitEnd()

        val jmhParams: List<Pair<String, String>> =
                fvs.filter { it.isParam() }.flatMap { fv ->
                    fv.params().flatMap { (name, values) ->
                        values.map { value ->
                            Pair(name, value)
                        }
                    }
                }

        mvs.forEach { m ->
            val oParams = descriptorToParamList(m.descriptor)
            val params: List<String> = if (!oParams.isEmpty()) {
                oParams.get()
            } else {
                listOf()
            }

            if (m.isBench()) {
                val bench = Benchmark(
                        clazz = className,
                        name = m.name,
                        params = params,
                        jmhParams = jmhParams
                )

                benchs.add(bench)

                val execInfo = m.execInfo()
                if (execInfo.isDefined()) {
                    benchExecInfos[bench] = execInfo.get()
                }
            } else if (m.isSetup()) {
                val setup = SetupMethod(
                        clazz = className,
                        name = m.name,
                        params = params
                )

                setups.add(setup)
            } else if (m.isTearDown()) {
                val tearDown = TearDownMethod(
                        clazz = className,
                        name = m.name,
                        params = params
                )

                tearDowns.add(tearDown)
            }
        }

        setClassExecInfo()
    }

    private fun setClassExecInfo() {
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

        classExecConfig = if (benchs.isEmpty()) {
            Option.empty()
        } else {
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
        }
    }
}
