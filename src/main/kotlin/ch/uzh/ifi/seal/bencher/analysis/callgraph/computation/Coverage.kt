package ch.uzh.ifi.seal.bencher.analysis.callgraph.computation

import ch.uzh.ifi.seal.bencher.Method

class Coverage(
    val of: Method,
    private val unitResults: Set<CoverageUnitResult>
) : CoverageComputation {

    private val unitResultsWithoutDuplicates: Set<CoverageUnitResult>
    private val unitsToUnitResults: Map<Method, CoverageUnitResult>

    init {
        val sorted = unitResults.sortedWith(CoverageUnitResultComparator)
        val selected = mutableSetOf<Method>()

        val withoutDuplicates = mutableSetOf<CoverageUnitResult>()
        val unitsToUnitResults = mutableMapOf<Method, CoverageUnitResult>()

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

    override fun single(of: Method, unit: Method): CoverageUnitResult {
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

    private fun map(from: Method, to: Method, r: CoverageUnitResult): CoverageUnitResult =
            when (r) {
                is Covered -> CUF.covered(
                        of = from,
                        unit = r.unit,
                        level = r.level
                )
                is PossiblyCovered -> CUF.possiblyCovered(
                        of = from,
                        unit = r.unit,
                        level = r.level,
                        probability = r.probability
                )
                is NotCovered -> CUF.notCovered(
                        of = from,
                        unit = to
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
        if (unitResults != other.unitResults) return false

        return true
    }

    override fun hashCode(): Int {
        var result = of.hashCode()
        result = 31 * result + unitResults.hashCode()
        return result
    }

    override fun toString(): String {
        return "Coverage(of=$of, unitResults=$unitResults)"
    }
}
