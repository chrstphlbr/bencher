package ch.uzh.ifi.seal.bencher.analysis.callgraph.reachability

import ch.uzh.ifi.seal.bencher.MethodComparator

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
                r1 is NotReachable && r2 is NotReachable -> compare(r1, r2)
                r1 is PossiblyReachable && r2 is PossiblyReachable -> compare(r1, r2)
                r1 is Reachable && r2 is Reachable -> compare(r1, r2)
                else -> throw IllegalArgumentException("r1 and r2 not of same type: ${r1::class} != ${r2::class}")
            }

    private fun compare(r1: PossiblyReachable, r2: PossiblyReachable): Int = pc.compare(r1, r2)

    private fun compare(r1: Reachable, r2: Reachable): Int = rc.compare(r1, r2)

    private fun compare(r1: NotReachable, r2: NotReachable): Int = nc.compare(r1, r2)
}
