package ch.uzh.ifi.seal.bencher.analysis.finder.shared

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.right
import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import ch.uzh.ifi.seal.bencher.analysis.finder.BenchmarkFinder
import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration

abstract class AbstractBenchmarkFinder : BenchmarkFinder {
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
            return Either.Right(benchmarkExecutionInfos)
        }

        val eBenchs = all()
        eBenchs.getOrElse {
            return Either.Left(it)
        }.right()

        return Either.Right(benchmarkExecutionInfos)
    }

    override fun classExecutionInfos(): Either<String, Map<Class, ExecutionConfiguration>> {
        if (parsed) {
            return Either.Right(classExecutionInfos)
        }

        val eBenchs = all()
        eBenchs.getOrElse {
            return Either.Left(it)
        }.right()

        return Either.Right(classExecutionInfos)
    }

    fun saveExecInfos(className: String, benchClass: BenchClass) {
        val c = Class(name = className)

        benchClass.classExecConfig.map {
            classExecutionInfos[c] = it
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
            r.getOrElse {
                return Either.Left(it)
            }.right()
        }

        return Either.Right(som.all())
    }
}