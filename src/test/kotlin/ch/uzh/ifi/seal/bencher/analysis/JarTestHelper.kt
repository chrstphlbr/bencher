package ch.uzh.ifi.seal.bencher.analysis

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.PlainMethod

object JarTestHelper {
    val jar2BenchsJmh110 = "benchmarks_2_jmh110.jar"
    val jar4BenchsJmh110 = "benchmarks_4_jmh110.jar"
    val jar2BenchsJmh121 = "benchmarks_2_jmh121.jar"
    val jar4BenchsJmh121 = "benchmarks_4_jmh121.jar"
    val jar4BenchsJmh121v2 = "benchmarks_4_jmh121_v2.jar"

    object BenchNonParameterized {
        val fqn = "org.sample.BenchNonParameterized"
        val bench2 = Benchmark(clazz = fqn, name ="bench2", params = listOf(), jmhParams = listOf())
    }

    object BenchParameterized {
        val fqn = "org.sample.BenchParameterized"
        val bench1 = Benchmark(clazz = fqn, name = "bench1", params = listOf(), jmhParams = listOf(Pair("str", "1"), Pair("str", "2"), Pair("str", "3")))
        val setup = PlainMethod(clazz = fqn, name = "setup", params = listOf())
    }

    object BenchParameterized2 {
        val fqn = "org.sample.BenchParameterized2"
        val bench4 = Benchmark(clazz = fqn, name = "bench4", params = listOf(), jmhParams = listOf(Pair("str", "1"), Pair("str", "2"), Pair("str", "3")))
        val setup = PlainMethod(clazz = fqn, name = "setup", params = listOf())
    }

    object OtherBench {
        val fqn = "org.sample.OtherBench"
        val bench3 = Benchmark(clazz = fqn, name = "bench3", params = listOf(), jmhParams = listOf())
        val setup = PlainMethod(clazz = fqn, name = "setup", params = listOf())
        val tearDown = PlainMethod(clazz = fqn, name = "teardown", params = listOf())
    }

    object CoreA {
        val fqn = "org.sample.core.CoreA"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf("java.lang.String", "org.sample.core.CoreI"))
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf())
    }

    object CoreB {
        val fqn = "org.sample.core.CoreB"
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf())
    }

    object CoreC {
        val fqn = "org.sample.core.CoreC"
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf())
    }

    object CoreD {
        val fqn = "org.sample.core.CoreD"
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf())
    }
}
