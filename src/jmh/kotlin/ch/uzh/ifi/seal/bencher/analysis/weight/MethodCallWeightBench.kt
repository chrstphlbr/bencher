package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.MF
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.RF
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Reachabilities
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Reachability
import org.openjdk.jmh.annotations.*
import kotlin.random.Random

@State(Scope.Benchmark)
open class MethodCallWeightBench {

    private val maxTos = 10000
    private val start = MF.benchmark(
            clazz = "c",
            name = "b",
            params = listOf("org.openjdk.jmh.infra.Blackhole"),
            jmhParams = listOf()
    )
    private val acc: (Double, Double) -> Double = Double::plus

    @Param(value = ["100", "1000", "10000"])
    var nrReachabilities: Int = 10000

    @Param(value = ["100", "1000", "10000"])
    var nrMethodWeights: Int = 10000

    @Param(value = ["10", "100", "1000"])
    var nrExclusions: Int = 1000

    private lateinit var tos: List<Method>
    private lateinit var ttos: List<Method>

    private lateinit var r: Reachability
    private lateinit var mws: MethodWeights
    private lateinit var exs: Set<Method>

    @Setup(Level.Trial)
    fun setupTrial() {
        tos = (1..maxTos).map { i ->
            MF.plainMethod(
                    clazz = "c$i",
                    name = "m$i",
                    params = listOf("a", "b", "c")
            )
        }

        ttos = tos.take(nrReachabilities)

        r = Reachabilities(
                start = start,
                reachabilities = ttos
                        .map { to ->
                            RF.reachable(
                                    from = start,
                                    to = to,
                                    level = 2
                            )
                        }
                        .toSet()
        )

        mws = tos.asSequence()
            .take(nrMethodWeights)
            .associateWith { 1.0 }
    }

    @Setup(Level.Iteration)
    fun setupIter() {
        val mexs = mutableSetOf<Method>()
        while (mexs.size < nrExclusions) {
            val idx = Random.nextInt(0, nrReachabilities)
            val to = ttos[idx]
            if (!mexs.contains(to)) {
                mexs.add(to)
            }
        }
        exs = mexs
    }

    @Benchmark
    fun methodWeightsFirst(): Pair<Double, Set<Method>> {
        return mcwMethodWeightsFirst(
                method = start,
                reachability = r,
                methodWeights = mws,
                exclusions = exs,
                accumulator = acc
        )
    }

    @Benchmark
    fun reachabilitiesFirst(): Pair<Double, Set<Method>> {
        return mcwReachabilitiesFirst(
                method = start,
                reachability = r,
                methodWeights = mws,
                exclusions = exs,
                accumulator = acc
        )
    }
}
