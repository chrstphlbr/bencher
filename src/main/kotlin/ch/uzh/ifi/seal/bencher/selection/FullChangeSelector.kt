package ch.uzh.ifi.seal.bencher.selection

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import ch.uzh.ifi.seal.bencher.analysis.ByteCodeConstants
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.change.*
import org.funktionale.either.Either

class FullChangeSelector(
        private val cgResult: CGResult,
        private val changes: Set<Change>
) : Selector {

    override fun select(benchs: Iterable<Benchmark>): Either<String, Iterable<Benchmark>> =
            Either.right(benchs.filter { select(it, changes, cgResult) })

    private fun select(benchmark: Benchmark, changes: Set<Change>, cgResult: CGResult): Boolean =
            changes.any { select(benchmark, it, cgResult) }

    private fun select(benchmark: Benchmark, change: Change, cgResult: CGResult): Boolean =
            methodChanged(benchmark, change) ||
                    changeInCalledMethod(benchmark, change, cgResult)

    // returns true iff
    //  (1) method has changed,
    //  (2) the method's class constructor or static initializer has changed
    //  (3) the method's setup methods have changed (if available)
    //  (4) the method's teardown methods have changed (if available)
    //  (5) a field in the containing class has changed, or
    //  (6) the class header (including annotations, e.g., how the benchmark is run)
    //  (7) an affected field, initializer, class was removed or introduced
    private fun methodChanged(m: Method, c: Change): Boolean =
            when (c) {
                // (1), (2), (3), (4)
                is MethodChange -> isMethodConstructorSetupOrTearDown(m, c.method)
                // (5)
                is ClassFieldChange -> c.clazz.name == m.clazz
                // (6)
                is ClassHeaderChange -> c.clazz.name == m.clazz
                // (1), (2), (3), (4)
                is ClassMethodChange -> isMethodConstructorSetupOrTearDown(m, c.method)
                // (7)
                is DeletionChange -> methodChanged(m, c.type)
                // (7)
                is AdditionChange -> methodChanged(m, c.type)
            }

    private fun isMethodConstructorSetupOrTearDown(m: Method, changedMethod: Method): Boolean =
            if (m == changedMethod) {
                // (1)
                true
            } else if (m.clazz == changedMethod.clazz) {
                if (changedMethod.name == ByteCodeConstants.constructor || changedMethod.name == ByteCodeConstants.staticInit) {
                    // (2)
                    true
                } else if (changedMethod is SetupMethod) {
                    // (3)
                    true
                } else if (changedMethod is TearDownMethod) {
                    // (4)
                    true
                } else {
                    false
                }
            } else {
                false
            }

    // returns true iff
    //  (1) the benchmark b exists in the call graph,
    //  (2) the change is a MethodChange and this method is reachable from b
    //  (3) the change affects (variable changed, constructor changed) the reachable method
    private fun changeInCalledMethod(b: Benchmark, c: Change, cgResult: CGResult): Boolean {
        // (1)
        val calls = cgResult.calls[b] ?: return false
        // (2), (3)
        return calls.reachabilities(true).any { mc -> methodChanged(mc.to, c) }
    }
}
