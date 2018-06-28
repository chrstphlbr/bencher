package ch.uzh.ifi.seal.Bencher.callgraph.dynamic

import ch.uzh.ifi.seal.Bencher.callgraph.CGExecutor
import ch.uzh.ifi.seal.Bencher.callgraph.CGResult

class AspectJCGTracer(val jar: String) : CGExecutor {

    override fun get(): CGResult {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        // compile aspect

        // weave aspect

        // get all benchmarks

        // execute benchmarks

        return CGResult(
                calls = listOf()
        )
    }

}
