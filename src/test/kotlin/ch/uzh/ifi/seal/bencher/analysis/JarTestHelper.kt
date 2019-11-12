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
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench2 = Benchmark(clazz = fqn, name = "bench2", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
    }

    object BenchParameterized {
        val fqn = "org.sample.BenchParameterized"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench1 = Benchmark(clazz = fqn, name = "bench1", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf(Pair("str", "1"), Pair("str", "2"), Pair("str", "3")))
        val setup = SetupMethod(clazz = fqn, name = "setup", params = listOf(), returnType = SourceCodeConstants.void)
    }

    object BenchParameterized2 {
        val fqn = "org.sample.BenchParameterized2"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench4 = Benchmark(clazz = fqn, name = "bench4", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf(Pair("str", "1"), Pair("str", "2"), Pair("str", "3")))
        val setup = SetupMethod(clazz = fqn, name = "setup", params = listOf(), returnType = SourceCodeConstants.void)
    }

    object BenchParameterized2v2 {
        val fqn = "org.sample.BenchParameterized2"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench4 = Benchmark(clazz = fqn, name = "bench4", params = listOf("org.openjdk.jmh.infra.Blackhole"), returnType = SourceCodeConstants.void, jmhParams = listOf(Pair("str", "1"), Pair("str", "2"), Pair("str", "3"), Pair("str2", "1"), Pair("str2", "2"), Pair("str2", "3")))
        val setup = SetupMethod(clazz = fqn, name = "setup", params = listOf(), returnType = SourceCodeConstants.void)
    }

    object OtherBench {
        val fqn = "org.sample.OtherBench"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench3 = Benchmark(clazz = fqn, name = "bench3", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
        val setup = SetupMethod(clazz = fqn, name = "setup", params = listOf(), returnType = SourceCodeConstants.void)
        val tearDown = TearDownMethod(clazz = fqn, name = "teardown", params = listOf(), returnType = SourceCodeConstants.void)
    }

    object NestedBenchmark {
        val fqn = "org.sample.NestedBenchmark"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench2 = Benchmark(clazz = fqn, name = "bench2", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())

        object Bench1 {
            val fqn = NestedBenchmark.fqn + "\$Bench1"
            val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
            val bench11 = Benchmark(clazz = fqn, name = "bench11", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
            val bench12 = Benchmark(clazz = fqn, name = "bench12", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
        }

        object Bench3 {
            val fqn = NestedBenchmark.fqn + "\$Bench3"
            val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
            val bench31 = Benchmark(clazz = fqn, name = "bench31", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())

            object Bench32 {
                val fqn = Bench3.fqn + "\$Bench32"
                val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
                val bench321 = Benchmark(clazz = fqn, name = "bench321", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
            }
        }
    }

    object BenchsWithGroup {
        val fqn = "org.sample.BenchsWithGroup"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench1 = Benchmark(clazz = fqn, name = "bench1", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf(), group = "groupName")
        val bench2 = Benchmark(clazz = fqn, name = "bench2", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf(), group = "groupName")
        val bench3 = Benchmark(clazz = fqn, name = "bench3", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
        val setup = SetupMethod(clazz = fqn, name = "setup", params = listOf(), returnType = SourceCodeConstants.void)
    }

    object BenchsStateObj {
        val fqn = "org.sample.BenchsWithStateObj"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench1 = Benchmark(clazz = fqn, name = "bench1", params = listOf("org.sample.stateObj.ObjectA"), returnType = SourceCodeConstants.void, jmhParams = listOf(Pair("str1", "1"), Pair("str1", "2"), Pair("str2", "1"), Pair("str4", "5")))
        val bench2 = Benchmark(clazz = fqn, name = "bench2", params = listOf("org.sample.stateObj.ObjectA", "org.sample.stateObj.ObjectB"), returnType = SourceCodeConstants.void, jmhParams = listOf(Pair("str1", "1"), Pair("str1", "2"), Pair("str2", "1"), Pair("str3", "1"), Pair("str4", "5")))
        val bench3 = Benchmark(clazz = fqn, name = "bench3", params = listOf("org.sample.stateObj.ObjectB", "org.sample.stateObj.ObjectA"), returnType = SourceCodeConstants.void, jmhParams = listOf(Pair("str1", "3"), Pair("str1", "4"), Pair("str3", "1"), Pair("str2", "1"), Pair("str4", "5")))
    }

    object CoreI {
        val fqn = "org.sample.core.CoreI"
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf(), returnType = SourceCodeConstants.void)
    }

    object CoreA {
        val fqn = "org.sample.core.CoreA"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf("java.lang.String", "org.sample.core.CoreI"), returnType = SourceCodeConstants.void)
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf(), returnType = SourceCodeConstants.void)
    }

    object CoreB {
        val fqn = "org.sample.core.CoreB"
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf(), returnType = SourceCodeConstants.void)
    }

    object CoreC {
        val fqn = "org.sample.core.CoreC"
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf(), returnType = SourceCodeConstants.void)
    }

    object CoreD {
        val fqn = "org.sample.core.CoreD"
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf(), returnType = SourceCodeConstants.void)
    }

    object CoreE {
        val fqn = "org.sample.core.CoreE"
        val mn_1 = PlainMethod(clazz = fqn, name = "mn", params = listOf(), returnType = SourceCodeConstants.void)
        val mn_2 = PlainMethod(clazz = fqn, name = "mn", params = listOf("java.lang.String", "java.lang.String[]"), returnType = SourceCodeConstants.void)
        val mn_3 = PlainMethod(clazz = fqn, name = "mn", params = listOf("int", "java.lang.String"), returnType = SourceCodeConstants.void)
        val mn1_1 = PlainMethod(clazz = fqn, name = "mn1", params = listOf("java.lang.String", "java.lang.String[]"), returnType = SourceCodeConstants.void)
        val mn1_2 = PlainMethod(clazz = fqn, name = "mn1", params = listOf("java.lang.String", "java.util.List"), returnType = SourceCodeConstants.void)
        val mn2 = PlainMethod(clazz = fqn, name = "mn2", params = listOf("int", "java.lang.String", "java.lang.String[]"), returnType = SourceCodeConstants.void)
    }

    object ObjectA {
        val fqn = "org.sample.stateObj.ObjectA"
    }

    object ObjectB {
        val fqn = "org.sample.stateObj.ObjectB"
    }
}
