package ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability

import ch.uzh.ifi.seal.bencher.Method
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

interface ReachabilityFactory {
    fun reachable(from: Method, to: Method, level: Int): Reachable
    fun possiblyReachable(from: Method, to: Method, level: Int, probability: Double): PossiblyReachable
    fun notReachable(from: Method, to: Method): NotReachable
}

object RF : ReachabilityFactory {
    private val rs = mutableSetOf<Reachable>()
    private val rl = ReentrantReadWriteLock()

    override fun reachable(from: Method, to: Method, level: Int): Reachable =
            rl.write {
                val f = rs.find {
                    it.from == from &&
                            it.to == to &&
                            it.level== level
                }
                if (f == null) {
                    val n = Reachable(
                            from = from,
                            to = to,
                            level = level
                    )
                    rs.add(n)
                    n
                } else {
                    f
                }
            }

    private val ps = mutableSetOf<PossiblyReachable>()
    private val pl = ReentrantReadWriteLock()

    override fun possiblyReachable(from: Method, to: Method, level: Int, probability: Double): PossiblyReachable =
            pl.write {
                val f = ps.find {
                    it.from == from &&
                            it.to == to &&
                            it.level == level &&
                            it.probability == probability
                }
                if (f == null) {
                    val n = PossiblyReachable(
                            from = from,
                            to = to,
                            level = level,
                            probability = probability
                    )
                    ps.add(n)
                    n
                } else {
                    f
                }
            }

    private val ns = mutableSetOf<NotReachable>()
    private val nl = ReentrantReadWriteLock()

    override fun notReachable(from: Method, to: Method): NotReachable =
            nl.write {
                val f = ns.find { it.from == from && it.to == to }
                if (f == null) {
                    val n = NotReachable(from = from, to = to)
                    ns.add(n)
                    n
                } else {
                    f
                }
            }
}
