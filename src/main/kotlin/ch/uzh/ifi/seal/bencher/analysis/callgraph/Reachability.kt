package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.MethodComparator
import ch.uzh.ifi.seal.bencher.analysis.descriptorToParamList
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

interface Reachability {
    fun reachable(from: Method, to: Method): ReachabilityResult
    fun reachable(from: Method, ms: Iterable<Method>, excludeNotReachable: Boolean = true): Iterable<ReachabilityResult> =
            ms.mapNotNull {
                val r = reachable(from, it)
                if (excludeNotReachable && r is NotReachable) {
                    null
                } else {
                    r
                }
            }
    fun reachabilities(removeDuplicateTos: Boolean = false): Set<ReachabilityResult>
}

sealed class ReachabilityResult(
        open val from: Method,
        open val to: Method
)
data class NotReachable(
        override val from: Method,
        override val to: Method
) : ReachabilityResult(from, to)
data class Reachable(
        override val from: Method,
        override val to: Method,
        val level: Int
) : ReachabilityResult(from, to)
data class PossiblyReachable(
        override val from: Method,
        override val to: Method,
        val level: Int,
        val probability: Double
) : ReachabilityResult(from, to)

object ReachabilityResultComparator : Comparator<ReachabilityResult> {
    private val pc = compareBy(MethodComparator, PossiblyReachable::to)
            .thenBy(MethodComparator, PossiblyReachable::from)
            .thenByDescending(PossiblyReachable::probability)
            .thenBy(PossiblyReachable::level)

    private val nc = compareBy(MethodComparator, NotReachable::to)
            .thenBy(MethodComparator, NotReachable::from)

    private val rc = compareBy(MethodComparator, Reachable::to)
            .thenBy(MethodComparator, Reachable::from)
            .thenBy(Reachable::level)


    override fun compare(r1: ReachabilityResult, r2: ReachabilityResult): Int {
        if (r1 == r2) {
            return 0
        }

        if (r1::class == r2::class) {
            return compareSameClass(r1, r2)
        }

        if (r1 !is NotReachable && r2 is NotReachable) {
            return -1
        } else if (r1 is NotReachable && r2 !is NotReachable) {
            return 1
        }

        if (r1 !is PossiblyReachable && r2 is PossiblyReachable) {
            return -1
        } else if (r1 is PossiblyReachable && r2 !is PossiblyReachable) {
            return 1
        }

        if (r1 !is NotReachable && r2 is NotReachable) {
            return -1
        } else if (r1 is NotReachable && r2 !is NotReachable) {
            return 1
        }

        throw IllegalStateException("Can not handle r1: ${r1::class}; r2: ${r2::class}")
    }

    private fun compareSameClass(r1: ReachabilityResult, r2: ReachabilityResult): Int =
            when  {
                r1 is NotReachable && r2 is NotReachable-> compare(r1, r2)
                r1 is PossiblyReachable && r2 is PossiblyReachable -> compare(r1, r2)
                r1 is Reachable && r2 is Reachable -> compare(r1, r2)
                else -> throw IllegalArgumentException("r1 and r2 not of same type: ${r1::class} != ${r2::class}")
            }

    private fun compare(r1: PossiblyReachable, r2: PossiblyReachable): Int = pc.compare(r1, r2)

    private fun compare(r1: Reachable, r2: Reachable): Int = rc.compare(r1, r2)

    private fun compare(r1: NotReachable, r2: NotReachable): Int = nc.compare(r1, r2)
}

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

class Reachabilities(
        val start: Method,
        private val reachabilities: Set<ReachabilityResult>
) : Reachability {

    private val reachabilitiesNoDuplicates: Set<ReachabilityResult> = {
        val srs = reachabilities.sortedWith(ReachabilityResultComparator)
        val selected = mutableSetOf<Method>()
        srs.filter {
            if (selected.contains(it.to)) {
                false
            } else {
                selected.add(it.to)
                true
            }
        }.toSet()
    }()

    private val tosRR: Map<Method, ReachabilityResult> = reachabilitiesNoDuplicates.associateBy { it.to }

    override fun reachable(from: Method, to: Method): ReachabilityResult {
        if (from != start || !tosRR.containsKey(to)) {
            return RF.notReachable(from, to)
        }

        val rr = tosRR[to]
        return map(from, rr) ?: RF.notReachable(from, to)
    }

    private fun map(from: Method, r: ReachabilityResult?): ReachabilityResult? =
            when (r) {
                null -> null
                is NotReachable -> null
                is Reachable -> RF.reachable(
                        from = from,
                        to = r.to,
                        level = r.level
                )
                is PossiblyReachable -> RF.possiblyReachable(
                        from = from,
                        to = r.to,
                        level = r.level,
                        probability = r.probability
                )
            }

    override fun reachabilities(removeDuplicateTos: Boolean): Set<ReachabilityResult> =
            if (!removeDuplicateTos) {
                reachabilities
            } else {
                reachabilitiesNoDuplicates
            }

    fun union(other: Reachabilities): Reachabilities =
            if (start != other.start) {
                throw IllegalArgumentException("Can not create union: start vertices not equal ($start != ${other.start})")
            } else {
                Reachabilities(
                        start = start,
                        reachabilities = reachabilities.union(other.reachabilities)
                )
            }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Reachabilities

        if (start != other.start) return false
        if (reachabilities != other.reachabilities) return false

        return true
    }

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + reachabilities.hashCode()
        return result
    }

    override fun toString(): String {
        return "Reachabilities(start=$start, reachabilities=$reachabilities)"
    }
}
