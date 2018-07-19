package ch.uzh.ifi.seal.bencher.analysis

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.PlainMethod

object JarHelper {
    val jar2BenchsJmh110 = "benchmarks_2_jmh110.jar"
    val jar4BenchsJmh110 = "benchmarks_4_jmh110.jar"
    val jar2BenchsJmh121 = "benchmarks_2_jmh121.jar"
    val jar4BenchsJmh121 = "benchmarks_4_jmh121.jar"

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
        val m = PlainMethod(clazz = "org.sample.core.CoreA", name = "m", params = listOf())
    }

    object CoreB {
        val m = PlainMethod(clazz = "org.sample.core.CoreB", name = "m", params = listOf())
    }

    object CoreC {
        val m = PlainMethod(clazz = "org.sample.core.CoreC", name = "m", params = listOf())
    }

    object CoreD {
        val m = PlainMethod(clazz = "org.sample.core.CoreD", name = "m", params = listOf())
    }
}
