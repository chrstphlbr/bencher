package ch.uzh.ifi.seal.bencher.analysis.finder.shared

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import ch.uzh.ifi.seal.bencher.analysis.finder.BenchmarkFinder
import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration
import org.funktionale.either.Either

abstract class BenchFinder : BenchmarkFinder {
    protected var parsed: Boolean = false
    protected lateinit var som: StateObjectManager

    protected var benchs: MutableList<Benchmark> = mutableListOf()
    protected val setups: MutableMap<Benchmark, Set<SetupMethod>> = mutableMapOf()
    protected val tearDowns: MutableMap<Benchmark, Set<TearDownMethod>> = mutableMapOf()

    // execution infos
    protected val benchmarkExecutionInfos: MutableMap<Benchmark, ExecutionConfiguration> = mutableMapOf()
    protected val classExecutionInfos: MutableMap<Class, ExecutionConfiguration> = mutableMapOf()

    override fun setups(b: Benchmark): Collection<SetupMethod> = setups[b] ?: setOf()

    override fun tearDowns(b: Benchmark): Collection<TearDownMethod> = tearDowns[b] ?: setOf()

    override fun benchmarkExecutionInfos(): Either<String, Map<Benchmark, ExecutionConfiguration>> {
        if (parsed) {
            return Either.right(benchmarkExecutionInfos)
        }

        val eBenchs = all()
        if (eBenchs.isLeft()) {
            return Either.left(eBenchs.left().get())
        }

        return Either.right(benchmarkExecutionInfos)
    }

    override fun classExecutionInfos(): Either<String, Map<Class, ExecutionConfiguration>> {
        if (parsed) {
            return Either.right(classExecutionInfos)
        }

        val eBenchs = all()
        if (eBenchs.isLeft()) {
            return Either.left(eBenchs.left().get())
        }

        return Either.right(classExecutionInfos)
    }

    fun saveExecInfos(className: String, benchClass: BenchClass) {
        val c = Class(name = className)

        val classExecInfo = benchClass.classExecConfig
        if (classExecInfo.isDefined()) {
            classExecutionInfos[c] = classExecInfo.get()
        }

        val b = benchClass.benchs.toList()
        benchs.addAll(b)

        val benchExecInfos = benchClass.benchExecInfos
        b.forEach {
            setups[it] = benchClass.setups
            tearDowns[it] = benchClass.tearDowns
            val bei = benchExecInfos[it]
            if (bei != null) {
                benchmarkExecutionInfos[it] = bei
            }
        }
    }

    override fun jmhParamSource(b: Benchmark): Map<String, String> {
        val ret = mutableMapOf<String, String>()

        val jhmParamsName = b.jmhParams.map { it.first }.distinct()
        jhmParamsName.forEach outer@{ jmhParamName ->
            // check all method argument types if jmh param is there defined
            b.params.forEach { methodArgumentType ->
                if (som.hasStateObjectJmhParam(methodArgumentType, jmhParamName)) {
                    ret[jmhParamName] = methodArgumentType
                    return@outer
                }
            }

            // else its a jmh param of the own class
            ret[jmhParamName] = b.clazz
        }

        return ret
    }

    override fun stateObj(): Either<String, Map<String, Map<String, MutableList<String>>>> {
        if (!parsed) {
            val r = all()
            if (r.isLeft()) {
                return Either.left(r.left().get())
            }
        }

        return Either.right(som.all())
    }
}