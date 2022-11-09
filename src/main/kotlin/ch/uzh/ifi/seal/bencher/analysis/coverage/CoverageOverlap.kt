package ch.uzh.ifi.seal.bencher.analysis.coverage

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.Coverage
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.CoverageUnit

interface CoverageOverlap {

    fun overlapping(m1: Method, m2: Method): Boolean = overlappingPercentage(m1, m2) != 0.0

    // overlappingPercentage returns the percentage of m1's covered methods that overlap with m2's methods.
    // note that overlappingPercentage(m1, m2) is not necessarily equal to overlappingPercentage(m2, m1)
    // if m1 or m2 do not have any covered methods, the result is 0.0
    fun overlappingPercentage(m1: Method, m2: Method): Double

    // overlappingPercentage returns the percentage of m's covered methods that overlap with
    // all other root methods (e.g benchmarks) of the coverage set
    // if m does not have any covered methods, the result is 0.0
    fun overlappingPercentage(m: Method): Double
}

class CoverageOverlapImpl(
    coverages: Iterable<Coverage>
) : CoverageOverlap {

    private val benchMatrixIDs: Map<Method, Int>
    private val unitMatrixIDs: Map<CoverageUnit, Int>

    private val coverageMatrix: Array<Array<Boolean>> // has benchmarks on the first level and their coverages on the second
    private val coverageMatrixTransposed: Array<Array<Boolean>>  // has coverages on the first level and benchmarks on the second
    private val coverageList: List<Coverage>

    private val opPair = mutableMapOf<Pair<Method, Method>, Double>()
    private val opAll = mutableMapOf<Method, Double>()

    init {
        val benchmarkIDs = mutableMapOf<Method, Int>()
        val unitIDs = mutableMapOf<CoverageUnit, Int>()

        coverageList = coverages.toList()

        coverageList.forEachIndexed { idx, r ->
            val  m = r.of

            if (benchmarkIDs.contains(m)) {
                throw IllegalStateException("method ($m$) already contained in coverages")
            }

            benchmarkIDs[m] = idx

            r.all(removeDuplicates = true).forEach {
                unitIDs.putIfAbsent(it.unit, unitIDs.size)
            }
        }

        if (benchmarkIDs.size != coverageList.size) {
            throw IllegalStateException("bmids.size (${benchmarkIDs.size}) != coverageList.size ${coverageList.size}")
        }

        coverageMatrix = Array(size = benchmarkIDs.size, init = { Array(size = unitIDs.size, init = { false }) })
        coverageMatrixTransposed = Array(size = unitIDs.size, init = { Array(size = benchmarkIDs.size, init = { false }) })

        benchmarkIDs.forEach { (b, idx) ->
            val listBench = coverageList[idx]
            if (b != listBench.of) {
                throw IllegalStateException("benchmark id and list out of sync: $b != ${listBench.of}")
            }

            listBench.all(removeDuplicates = true).forEach { coverageUnitResult ->
                val unit = coverageUnitResult.unit
                val unitID = unitIDs[unit] ?: throw IllegalStateException("no id in unitIDs for $unit")
                coverageMatrix[idx][unitID] = true
                coverageMatrixTransposed[unitID][idx] = true
            }
        }

        benchMatrixIDs = benchmarkIDs
        unitMatrixIDs = unitIDs
    }

    private fun computeOverlappingPercentage(m1: Method, m2: Method): Double {
        val idx1 = benchMatrixIDs[m1] ?: throw IllegalStateException("no id for $m1")
        val idx2 = benchMatrixIDs[m2] ?: throw IllegalStateException("no id for $m2")

        val us1 = coverageMatrix[idx1]
        val us1Size = us1.size
        val us2 = coverageMatrix[idx2]
        val us2Size = us2.size

        val umSize = unitMatrixIDs.size

        if (us1Size != us2Size) {
            throw IllegalStateException("coverage sizes not equal: $us1Size != $us2Size")
        }
        if (us1Size != umSize) {
            throw IllegalStateException("coverage sizes not equal to methodMatrixId.size: $us1Size != $umSize")
        }

        val overlaps = (0 until us1Size)
            .filter { i -> us1[i] && us1[i] == us2[i] }
            .size
            .toDouble()

        val m1CoveredMethods = us1
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
        val idx = benchMatrixIDs[m] ?: throw IllegalStateException("no id for $m")

        val rs = coverageMatrix[idx]

        val coveredMethods = rs
            .filter { it }
            .size

        val overlaps = coverageMatrixTransposed
            .asSequence()
            .filterIndexed { i, _ -> rs[i] } // only keep the coverages which the benchmark also covers
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
