package ch.uzh.ifi.seal.bencher.analysis.callgraph

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.MethodComparator
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write


data class MethodCall(
        val from: Method,
        val to: Method,
        val nrPossibleTargets: Int,
        val idPossibleTargets: Int
)

object MethodCallComparator : Comparator<MethodCall> {
    private val c = compareBy(MethodComparator, MethodCall::from)
            .thenBy(MethodCall::idPossibleTargets)
            .thenBy(MethodComparator, MethodCall::to)
            .thenBy(MethodCall::nrPossibleTargets)

    override fun compare(mc1: MethodCall, mc2: MethodCall): Int = c.compare(mc1, mc2)
}

interface MethodCallFactory {
    fun methodCall(from: Method, to: Method, idPossibleTargets: Int, nrPossibleTargets: Int): MethodCall
}

object MCF : MethodCallFactory {
    private val s = mutableSetOf<MethodCall>()
    private val l = ReentrantReadWriteLock()

    override fun methodCall(from: Method, to: Method, idPossibleTargets: Int, nrPossibleTargets: Int): MethodCall =
            l.write {
                val fmc = s.find {
                    it.from == from &&
                            it.to == to &&
                            it.idPossibleTargets == idPossibleTargets &&
                            it.nrPossibleTargets == nrPossibleTargets
                }
                if (fmc == null) {
                    val nmc = MethodCall(
                            from = from,
                            to = to,
                            idPossibleTargets = idPossibleTargets,
                            nrPossibleTargets = nrPossibleTargets
                    )
                    s.add(nmc)
                    nmc
                } else {
                    fmc
                }
            }
}
