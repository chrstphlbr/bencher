package ch.uzh.ifi.seal.bencher.analysis

import ch.uzh.ifi.seal.bencher.*

object JarTestHelper {
    val jar4BenchsJmh10 = "benchmarks_4_jmh10.jar"
    val jar2BenchsJmh110 = "benchmarks_2_jmh110.jar"
    val jar4BenchsJmh110 = "benchmarks_4_jmh110.jar"
    val jar2BenchsJmh121 = "benchmarks_2_jmh121.jar"
    val jar4BenchsJmh121 = "benchmarks_4_jmh121.jar"
    val jar4BenchsJmh121v2 = "benchmarks_4_jmh121_v2.jar"

    val jar2BenchsJmh121Version = JMHVersion(1, 21)
    val jar4BenchsJmh121Version = JMHVersion(1, 21)
    val jar2BenchsJmh110Version = JMHVersion(1, 10)
    val jar4BenchsJmh110Version = JMHVersion(1, 10)

    object BenchNonParameterized {
        val fqn = "org.sample.BenchNonParameterized"
        val bench2 = Benchmark(clazz = fqn, name = "bench2", params = listOf(), jmhParams = listOf())
    }

    object BenchParameterized {
        val fqn = "org.sample.BenchParameterized"
        val bench1 = Benchmark(clazz = fqn, name = "bench1", params = listOf(), jmhParams = listOf(Pair("str", "1"), Pair("str", "2"), Pair("str", "3")))
        val setup = SetupMethod(clazz = fqn, name = "setup", params = listOf())
    }

    object BenchParameterized2 {
        val fqn = "org.sample.BenchParameterized2"
        val bench4 = Benchmark(clazz = fqn, name = "bench4", params = listOf(), jmhParams = listOf(Pair("str", "1"), Pair("str", "2"), Pair("str", "3")))
        val setup = SetupMethod(clazz = fqn, name = "setup", params = listOf())
    }

    object OtherBench {
        val fqn = "org.sample.OtherBench"
        val bench3 = Benchmark(clazz = fqn, name = "bench3", params = listOf(), jmhParams = listOf())
        val setup = SetupMethod(clazz = fqn, name = "setup", params = listOf())
        val tearDown = TearDownMethod(clazz = fqn, name = "teardown", params = listOf())
    }

    object NestedBenchmark {
        val fqn = "org.sample.NestedBenchmark"
        val bench2 = Benchmark(clazz = fqn, name = "bench2", params = listOf(), jmhParams = listOf())

        object Bench1 {
            val fqn = NestedBenchmark.fqn + "\$Bench1"
            val bench11 = Benchmark(clazz = fqn, name = "bench11", params = listOf(), jmhParams = listOf())
            val bench12 = Benchmark(clazz = fqn, name = "bench12", params = listOf(), jmhParams = listOf())
        }

        object Bench3 {
            val fqn = NestedBenchmark.fqn + "\$Bench3"
            val bench31 = Benchmark(clazz = fqn, name = "bench31", params = listOf(), jmhParams = listOf())

            object Bench32 {
                val fqn = Bench3.fqn + "\$Bench32"
                val bench321 = Benchmark(clazz = fqn, name = "bench321", params = listOf(), jmhParams = listOf())
            }
        }
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
