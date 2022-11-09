package ch.uzh.ifi.seal.bencher.analysis.coverage.computation

import ch.uzh.ifi.seal.bencher.ID
import ch.uzh.ifi.seal.bencher.Method
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

interface CoverageUnitResultFactory {
    fun covered(of: Method, unit: CoverageUnit, level: Int): Covered
    fun possiblyCovered(of: Method, unit: CoverageUnit, level: Int, probability: Double): PossiblyCovered
    fun notCovered(of: Method, unit: CoverageUnit): NotCovered
}

object CUF : CoverageUnitResultFactory {
    private val rm = mutableMapOf<String, Covered>()
    private val rl = ReentrantReadWriteLock()

    override fun covered(of: Method, unit: CoverageUnit, level: Int): Covered =
            rl.write {
                val s = ID.string(of, unit, level)
                val f = rm[s]
                if (f == null) {
                    val n = Covered(
                            unit = unit,
                            level = level
                    )
                    rm[s] = n
                    n
                } else {
                    f
                }
            }

    private val pm = mutableMapOf<String, PossiblyCovered>()
    private val pl = ReentrantReadWriteLock()

    override fun possiblyCovered(of: Method, unit: CoverageUnit, level: Int, probability: Double): PossiblyCovered =
            pl.write {
                val s = ID.string(of, unit, level, probability)
                val f = pm[s]
                if (f == null) {
                    val n = PossiblyCovered(
                            unit = unit,
                            level = level,
                            probability = probability
                    )
                    pm[s] = n
                    n
                } else {
                    f
                }
            }

    private val nm = mutableMapOf<String, NotCovered>()
    private val nl = ReentrantReadWriteLock()

    override fun notCovered(of: Method, unit: CoverageUnit): NotCovered =
            nl.write {
                val s = ID.string(of, unit)
                val f = nm[s]
                if (f == null) {
                    val n = NotCovered(unit = unit)
                    nm[s] = n
                    n
                } else {
                    f
                }
            }
}
