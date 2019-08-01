package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.execution.ExecutionConfiguration
import org.junit.jupiter.api.Assertions

abstract class AbstractBenchExecInfoTest {

    protected fun assertClassConfig(cs: Map<Class, ExecutionConfiguration>, c: Class, ec: ExecutionConfiguration) {
        val cc = cs[c]
        if (cc == null) {
            Assertions.fail<String>("No class configuration in provided map for $c")
        }
        Assertions.assertTrue(cc == ec, "Execution configuration did not match expectation for $c.\nExpected: $ec\nWas $cc")
    }

    protected fun assertBenchConfig(cs: Map<Benchmark, ExecutionConfiguration>, b: Benchmark, ec: ExecutionConfiguration) {
        val bc = cs[b]
        if (bc == null) {
            Assertions.fail<String>("No benchmark configuration in provided map for $b")
        }
        Assertions.assertTrue(bc == ec, "Execution configuration did not match expectation for $b.\nExpected: $ec\nWas $bc")
    }
}
