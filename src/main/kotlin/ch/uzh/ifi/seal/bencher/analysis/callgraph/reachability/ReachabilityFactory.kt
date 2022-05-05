package ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability

import ch.uzh.ifi.seal.bencher.ID
import ch.uzh.ifi.seal.bencher.Method
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

interface ReachabilityFactory {
    fun reachable(from: Method, to: Method, level: Int): Reachable
    fun possiblyReachable(from: Method, to: Method, level: Int, probability: Double): PossiblyReachable
    fun notReachable(from: Method, to: Method): NotReachable
}

object RF : ReachabilityFactory {
    private val rm = mutableMapOf<String, Reachable>()
    private val rl = ReentrantReadWriteLock()

    override fun reachable(from: Method, to: Method, level: Int): Reachable =
            rl.write {
                val s = ID.string(from, to, level)
                val f = rm[s]
                if (f == null) {
                    val n = Reachable(
                            to = to,
                            level = level
                    )
                    rm[s] = n
                    n
                } else {
                    f
                }
            }

    private val pm = mutableMapOf<String, PossiblyReachable>()
    private val pl = ReentrantReadWriteLock()

    override fun possiblyReachable(from: Method, to: Method, level: Int, probability: Double): PossiblyReachable =
            pl.write {
                val s = ID.string(from, to, level, probability)
                val f = pm[s]
                if (f == null) {
                    val n = PossiblyReachable(
                            to = to,
                            level = level,
                            probability = probability
                    )
                    pm[s] = n
                    n
                } else {
                    f
                }
            }

    private val nm = mutableMapOf<String, NotReachable>()
    private val nl = ReentrantReadWriteLock()

    override fun notReachable(from: Method, to: Method): NotReachable =
            nl.write {
                val s = ID.string(from, to)
                val f = nm[s]
                if (f == null) {
                    val n = NotReachable(to = to)
                    nm[s] = n
                    n
                } else {
                    f
                }
            }
}
