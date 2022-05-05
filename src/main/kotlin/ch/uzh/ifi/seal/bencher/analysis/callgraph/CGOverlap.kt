package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability.Reachabilities

interface CGOverlap {

    fun overlapping(m1: Method, m2: Method): Boolean = overlappingPercentage(m1, m2) != 0.0

    // overlappingPercentage returns the percentage of m1's reachable methods that overlap with m2's methods.
    // note that overlappingPercentage(m1, m2) is not necessarily equal to overlappingPercentage(m2, m1)
    // if m1 or m2 do not have any reachable methods, the result is 0.0
    fun overlappingPercentage(m1: Method, m2: Method): Double

    // overlappingPercentage returns the percentage of m's reachable methods that overlap with
    // all other root methods (e.g benchmarks) of the call graph
    // if m does not have any reachable methods, the result is 0.0
    fun overlappingPercentage(m: Method): Double
}

class CGOverlapImpl(
    reachabilities: Iterable<Reachabilities>
) : CGOverlap {

    private val benchMatrixIds: Map<Method, Int>
    private val methodMatrixId: Map<Method, Int>

    private val reachabilityMatrix: Array<Array<Boolean>> // has benchmarks on the first level and their reachabilities on the second
    private val reachabilityMatrixTransposed: Array<Array<Boolean>>  // has reachabilities on the first level and benchmarks on the second
    private val reachabilityList: List<Reachabilities>

    private val opPair = mutableMapOf<Pair<Method, Method>, Double>()
    private val opAll = mutableMapOf<Method, Double>()

    init {
        val bmids = mutableMapOf<Method, Int>()
        val mmids = mutableMapOf<Method, Int>()

        reachabilityList = reachabilities.toList()

        reachabilityList.forEachIndexed { idx, r ->
            val  m = r.start

            if (bmids.contains(m)) {
                throw IllegalStateException("method ($m$) already contained in reachabilities")
            }

            bmids[m] = idx

            r.reachabilities(removeDuplicateTos = true).forEach {
                mmids.putIfAbsent(it.unit, mmids.size)
            }
        }

        if (bmids.size != reachabilityList.size) {
            throw IllegalStateException("bmids.size (${bmids.size}) != reachabilityList.size ${reachabilityList.size}")
        }

        reachabilityMatrix = Array(size = bmids.size, init = { Array(size = mmids.size, init = { false }) })
        reachabilityMatrixTransposed = Array(size = mmids.size, init = { Array(size = bmids.size, init = { false }) })

        bmids.forEach { (b, idx) ->
            val listBench = reachabilityList[idx]
            if (b != listBench.start) {
                throw IllegalStateException("benchmark id and list out of sync: $b != ${listBench.start}")
            }

            listBench.reachabilities(removeDuplicateTos = true).forEach { rr ->
                val to = rr.unit
                val rid = mmids[to] ?: throw IllegalStateException("no id in mmids for $to")
                reachabilityMatrix[idx][rid] = true
                reachabilityMatrixTransposed[rid][idx] = true
            }
        }

        benchMatrixIds = bmids
        methodMatrixId = mmids
    }

    private fun computeOverlappingPercentage(m1: Method, m2: Method): Double {
        val idx1 = benchMatrixIds[m1] ?: throw IllegalStateException("no id for $m1")
        val idx2 = benchMatrixIds[m2] ?: throw IllegalStateException("no id for $m2")

        val rs1 = reachabilityMatrix[idx1]
        val rs1Size = rs1.size
        val rs2 = reachabilityMatrix[idx2]
        val rs2Size = rs2.size

        val mmSize = methodMatrixId.size

        if (rs1Size != rs2Size) {
            throw IllegalStateException("reachabilities sizes not equal: $rs1Size != $rs2Size")
        }
        if (rs1Size != mmSize) {
            throw IllegalStateException("reachabilities sizes not equal to methodMatrixId.size: $rs1Size != $mmSize")
        }

        val overlaps = (0 until rs1Size)
            .filter { i -> rs1[i] && rs1[i] == rs2[i] }
            .size
            .toDouble()

        val m1CoveredMethods = rs1
            .filter { it }
            .size

        if (m1CoveredMethods == 0) {
            return 0.0
        }

        return overlaps / m1CoveredMethods
    }

    override fun overlappingPercentage(m1: Method, m2: Method): Double {
        val p = Pair(m1, m2)
        val m = opPair[p]
        return if (m == null) {
            val op = computeOverlappingPercentage(m1, m2)
            opPair[p] = op
            op
        } else {
            m
        }
    }

    private fun computeOverlappingPercentage(m: Method): Double {
        val idx = benchMatrixIds[m] ?: throw IllegalStateException("no id for $m")

        val rs = reachabilityMatrix[idx]

        val coveredMethods = rs
            .filter { it }
            .size

        val overlaps = reachabilityMatrixTransposed
            .asSequence()
            .filterIndexed { i, _ -> rs[i] } // only keep the reachabilities which the benchmark also covers
            .filter { benchs ->
                benchs
                    .asSequence()
                    .filterIndexed { bid, _ -> bid != idx } // remove benchmark
                    .any { it } // check if any of the other benchmarks also reaches this method
            }
            .toList()
            .size
            .toDouble()

        if (coveredMethods == 0) {
            return 0.0
        }

        return overlaps / coveredMethods
    }

    override fun overlappingPercentage(m: Method): Double {
        val mop = opAll[m]
        return if (mop == null) {
            val op = computeOverlappingPercentage(m)
            opAll[m] = op
            op
        } else {
            mop
        }
    }
}
