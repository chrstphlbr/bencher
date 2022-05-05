package ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability

import ch.uzh.ifi.seal.bencher.ID
import ch.uzh.ifi.seal.bencher.Method
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

interface ReachabilityFactory {
    fun reachable(from: Method, to: Method, level: Int): Covered
    fun possiblyReachable(from: Method, to: Method, level: Int, probability: Double): PossiblyCovered
    fun notReachable(from: Method, to: Method): NotCovered
}

object RF : ReachabilityFactory {
    private val rm = mutableMapOf<String, Covered>()
    private val rl = ReentrantReadWriteLock()

    override fun reachable(from: Method, to: Method, level: Int): Covered =
            rl.write {
                val s = ID.string(from, to, level)
                val f = rm[s]
                if (f == null) {
                    val n = Covered(
                            unit = to,
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

    override fun possiblyReachable(from: Method, to: Method, level: Int, probability: Double): PossiblyCovered =
            pl.write {
                val s = ID.string(from, to, level, probability)
                val f = pm[s]
                if (f == null) {
                    val n = PossiblyCovered(
                            unit = to,
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

    override fun notReachable(from: Method, to: Method): NotCovered =
            nl.write {
                val s = ID.string(from, to)
                val f = nm[s]
                if (f == null) {
                    val n = NotCovered(unit = to)
                    nm[s] = n
                    n
                } else {
                    f
                }
            }
}
