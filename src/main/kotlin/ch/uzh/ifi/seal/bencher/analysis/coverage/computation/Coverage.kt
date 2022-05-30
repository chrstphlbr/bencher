package ch.uzh.ifi.seal.bencher.analysis.coverage.computation

import ch.uzh.ifi.seal.bencher.Method

data class Coverage(
    val of: Method,
    private val unitResults: Set<CoverageUnitResult>
) : CoverageComputation {

    private val unitResultsWithoutDuplicates: Set<CoverageUnitResult>
    private val unitsToUnitResults: Map<CoverageUnit, CoverageUnitResult>

    init {
        val sorted = unitResults.sortedWith(CoverageUnitResultComparator)
        val selected = mutableSetOf<CoverageUnit>()

        val withoutDuplicates = mutableSetOf<CoverageUnitResult>()
        val unitsToUnitResults = mutableMapOf<CoverageUnit, CoverageUnitResult>()

        sorted.forEach {
            if (!selected.contains(it.unit)) {
                selected.add(it.unit)
                withoutDuplicates.add(it)
                unitsToUnitResults[it.unit] = it
            }
        }

        unitResultsWithoutDuplicates = withoutDuplicates
        this.unitsToUnitResults = unitsToUnitResults
    }

    override fun single(of: Method, unit: CoverageUnit): CoverageUnitResult {
        if (of != this.of || !unitsToUnitResults.containsKey(unit)) {
            return CUF.notCovered(of, unit)
        }

        val cur = unitsToUnitResults[unit]
        return if (cur != null) {
            map(of, unit, cur)
        } else {
            CUF.notCovered(of, unit)
        }
    }

    private fun map(of: Method, unit: CoverageUnit, cu: CoverageUnitResult): CoverageUnitResult =
        when (cu) {
            is Covered -> CUF.covered(
                of = of,
                unit = cu.unit,
                level = cu.level
            )
            is PossiblyCovered -> CUF.possiblyCovered(
                of = of,
                unit = cu.unit,
                level = cu.level,
                probability = cu.probability
            )
            is NotCovered -> CUF.notCovered(
                of = of,
                unit = unit
            )
        }

    override fun all(removeDuplicates: Boolean): Set<CoverageUnitResult> =
        if (!removeDuplicates) {
            unitResults
        } else {
            unitResultsWithoutDuplicates
        }

    fun union(other: Coverage): Coverage =
        if (of != other.of) {
            throw IllegalArgumentException("Can not create union: of units not equal ($of != ${other.of})")
        } else {
            Coverage(
                of = of,
                unitResults = unitResults.union(other.unitResults)
            )
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Coverage

        if (of != other.of) return false

        val ur1Sorted = this.unitResults.toSortedSet(CoverageUnitResultComparator)
        val ur2Sorted = other.unitResults.toSortedSet(CoverageUnitResultComparator)

        if (ur1Sorted.size != ur2Sorted.size) return false

        ur1Sorted.zip(ur2Sorted).forEach { (cur1, cur2) ->
            if (cur1 != cur2) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var result = of.hashCode()
        result = 31 * result + unitResults.hashCode()
        return result
    }
}
