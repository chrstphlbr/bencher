package ch.uzh.ifi.seal.bencher.analysis.sta

import ch.uzh.ifi.seal.bencher.analysis.JarBenchFinder
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaRTA
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.WalaSCG
import org.junit.jupiter.api.Test

class WalaSCGTest {

    @Test
    fun test() {
        val jar = "/Users/christophlaaber/tmp/benchmark-projects/Java/protostuff/protostuff-benchmarks/target/protostuff-benchmarks.jar"
        val e = WalaSCG(jar, JarBenchFinder(jar), WalaRTA())
        e.get()
    }

}
