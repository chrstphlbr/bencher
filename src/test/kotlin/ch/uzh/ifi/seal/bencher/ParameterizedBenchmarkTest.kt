package ch.uzh.ifi.seal.bencher

import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ParameterizedBenchmarkTest {

    private val bench = Benchmark(
            clazz = "a.a.A",
            params = listOf(),
            name = "bench",
            jmhParams = listOf(),
            returnType = SourceCodeConstants.void
    )

    @Test
    fun noParams() {
        val b = bench
        val pbs = b.parameterizedBenchmarks()

        Assertions.assertEquals(1, pbs.size)
        Assertions.assertEquals(b, pbs[0])
    }

    @Test
    fun idempotence() {
        val b = bench.copy(jmhParams = listOf(Pair("a", "1"), Pair("b", "1")))
        val pbs = b.parameterizedBenchmarks()
        Assertions.assertEquals(1, pbs.size)
        Assertions.assertEquals(pbs[0], b)
    }

    @Test
    fun oneParam() {
        val pv1 = Pair("a", "1")
        val pv2 = Pair("a", "2")
        val pv3 = Pair("a", "3")

        val b = bench.copy(jmhParams = listOf(pv1, pv2, pv3))
        val pbs = b.parameterizedBenchmarks()

        Assertions.assertEquals(3, pbs.size)
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv1)), pbs[0])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv2)), pbs[1])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv3)), pbs[2])
    }

    @Test
    fun oneParamRev() {
        val pv1 = Pair("a", "1")
        val pv2 = Pair("a", "2")
        val pv3 = Pair("a", "3")

        val b = bench.copy(jmhParams = listOf(pv1, pv2, pv3))
        val pbs = b.parameterizedBenchmarks(reversed = true)

        Assertions.assertEquals(3, pbs.size)
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv3)), pbs[0])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv2)), pbs[1])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv1)), pbs[2])
    }

    @Test
    fun twoParams() {
        val pv11 = Pair("a", "1")
        val pv12 = Pair("a", "2")
        val pv13 = Pair("a", "3")
        val pv21 = Pair("b", "1")
        val pv22 = Pair("b", "2")
        val pv23 = Pair("b", "3")

        val b = bench.copy(jmhParams = listOf(pv11, pv12, pv13, pv21, pv22, pv23))
        val pbs = b.parameterizedBenchmarks()

        Assertions.assertEquals(9, pbs.size)
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv21)), pbs[0])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv22)), pbs[1])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv23)), pbs[2])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv21)), pbs[3])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv22)), pbs[4])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv23)), pbs[5])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv21)), pbs[6])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv22)), pbs[7])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv23)), pbs[8])
    }

    @Test
    fun twoParamsRev() {
        val pv11 = Pair("a", "1")
        val pv12 = Pair("a", "2")
        val pv13 = Pair("a", "3")
        val pv21 = Pair("b", "1")
        val pv22 = Pair("b", "2")
        val pv23 = Pair("b", "3")

        val b = bench.copy(jmhParams = listOf(pv11, pv12, pv13, pv21, pv22, pv23))
        val pbs = b.parameterizedBenchmarks(reversed = true)

        Assertions.assertEquals(9, pbs.size)
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv23)), pbs[0])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv22)), pbs[1])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv21)), pbs[2])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv23)), pbs[3])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv22)), pbs[4])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv21)), pbs[5])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv23)), pbs[6])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv22)), pbs[7])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv21)), pbs[8])
    }

    @Test
    fun threeParams() {
        val pv11 = Pair("a", "1")
        val pv12 = Pair("a", "2")
        val pv13 = Pair("a", "3")
        val pv21 = Pair("b", "1")
        val pv22 = Pair("b", "2")
        val pv23 = Pair("b", "3")
        val pv31 = Pair("c", "1")
        val pv32 = Pair("c", "2")
        val pv33 = Pair("c", "3")

        val b = bench.copy(jmhParams = listOf(pv11, pv12, pv13, pv21, pv22, pv23, pv31, pv32, pv33))
        val pbs = b.parameterizedBenchmarks()

        Assertions.assertEquals(27, pbs.size)
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv21, pv31)), pbs[0])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv21, pv32)), pbs[1])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv21, pv33)), pbs[2])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv22, pv31)), pbs[3])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv22, pv32)), pbs[4])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv22, pv33)), pbs[5])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv23, pv31)), pbs[6])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv23, pv32)), pbs[7])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv23, pv33)), pbs[8])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv21, pv31)), pbs[9])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv21, pv32)), pbs[10])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv21, pv33)), pbs[11])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv22, pv31)), pbs[12])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv22, pv32)), pbs[13])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv22, pv33)), pbs[14])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv23, pv31)), pbs[15])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv23, pv32)), pbs[16])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv23, pv33)), pbs[17])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv21, pv31)), pbs[18])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv21, pv32)), pbs[19])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv21, pv33)), pbs[20])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv22, pv31)), pbs[21])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv22, pv32)), pbs[22])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv22, pv33)), pbs[23])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv23, pv31)), pbs[24])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv23, pv32)), pbs[25])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv23, pv33)), pbs[26])
    }

    @Test
    fun threeParamsRev() {
        val pv11 = Pair("a", "1")
        val pv12 = Pair("a", "2")
        val pv13 = Pair("a", "3")
        val pv21 = Pair("b", "1")
        val pv22 = Pair("b", "2")
        val pv23 = Pair("b", "3")
        val pv31 = Pair("c", "1")
        val pv32 = Pair("c", "2")
        val pv33 = Pair("c", "3")

        val b = bench.copy(jmhParams = listOf(pv11, pv12, pv13, pv21, pv22, pv23, pv31, pv32, pv33))
        val pbs = b.parameterizedBenchmarks(reversed = true)

        Assertions.assertEquals(27, pbs.size)
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv23, pv33)), pbs[0])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv23, pv32)), pbs[1])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv23, pv31)), pbs[2])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv22, pv33)), pbs[3])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv22, pv32)), pbs[4])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv22, pv31)), pbs[5])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv21, pv33)), pbs[6])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv21, pv32)), pbs[7])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv13, pv21, pv31)), pbs[8])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv23, pv33)), pbs[9])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv23, pv32)), pbs[10])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv23, pv31)), pbs[11])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv22, pv33)), pbs[12])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv22, pv32)), pbs[13])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv22, pv31)), pbs[14])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv21, pv33)), pbs[15])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv21, pv32)), pbs[16])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv12, pv21, pv31)), pbs[17])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv23, pv33)), pbs[18])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv23, pv32)), pbs[19])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv23, pv31)), pbs[20])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv22, pv33)), pbs[21])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv22, pv32)), pbs[22])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv22, pv31)), pbs[23])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv21, pv33)), pbs[24])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv21, pv32)), pbs[25])
        Assertions.assertEquals(b.copy(jmhParams = listOf(pv11, pv21, pv31)), pbs[26])
    }
}
