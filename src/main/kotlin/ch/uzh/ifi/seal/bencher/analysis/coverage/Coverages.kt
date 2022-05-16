package ch.uzh.ifi.seal.bencher.analysis.coverage

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.change.*
import ch.uzh.ifi.seal.bencher.analysis.coverage.computation.*


data class Coverages(
        val coverages: Map<Method, Coverage>
) : CoverageComputation, CoverageOverlap by CoverageOverlapImpl(coverages.values) {

    override fun single(of: Method, unit: CoverageUnit): CoverageUnitResult {
        val mcs = coverages[of] ?: return CUF.notCovered(of, unit)
        return mcs.single(of, unit)
    }

    override fun all(removeDuplicates: Boolean): Set<CoverageUnitResult> =
            coverages.flatMap { it.value.all(removeDuplicates) }.toSet()

    fun onlyChangedCoverages(
        changes: Set<Change>,
        methodChangeAssessment: MethodChangeAssessment = FullMethodChangeAssessment,
        lineChangeAssessment: LineChangeAssessment = LineChangeAssessmentImpl
    ): Coverages {
        val newCovs: Map<Method, Coverage> = coverages.mapValues { (_, cs) ->
            val newUnits = cs.all()
                .filter { cur ->
                    when (val u = cur.unit) {
                        is CoverageUnitMethod -> methodChangeAssessment.methodChanged(u.method, changes)
                        is CoverageUnitLine -> lineChangeAssessment.lineChanged(u.line, changes)
                    }
                }
                .toSet()
            Coverage(of = cs.of, unitResults = newUnits)
        }

        return Coverages(newCovs)
    }
}

fun Iterable<Coverages>.merge(): Coverages =
        this.fold(Coverages(mapOf())) { acc, cov -> merge(acc, cov) }


fun merge(cov1: Coverages, cov2: Coverages): Coverages {
    val c1 = cov1.coverages
    val c2 = cov2.coverages
    val intersectingKeys = c1.keys.intersect(c2.keys)
    if (intersectingKeys.isEmpty()) {
        // disjoint set of benchmarks -> return the union of the map
        return Coverages(
                coverages = c1 + c2
        )
    }

    // overlapping benchmark sets
    val newCovs = mutableMapOf<Method, Coverage>()
    // bc1 benchmarks that are not in bc2
    newCovs.putAll(c1.filterKeys { intersectingKeys.contains(it) })
    // bc2 benchmarks that are not in bc1
    newCovs.putAll(c2.filterKeys { intersectingKeys.contains(it) })
    // merge of benchmarks that are in both bc1 and bc2
    newCovs.putAll(
            intersectingKeys.map {
                Pair(it, c1.getValue(it).union(c2.getValue(it)))
            }
    )

    return Coverages(
            coverages = newCovs
    )
}
