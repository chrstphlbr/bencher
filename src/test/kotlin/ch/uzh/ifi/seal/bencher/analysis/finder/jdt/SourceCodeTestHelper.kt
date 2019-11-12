package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.PlainMethod
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import ch.uzh.ifi.seal.bencher.analysis.SourceCodeConstants

object SourceCodeTestHelper {
    val benchs2Jmh121 = "ch/uzh/ifi/seal/bencher/analysis/finder/jdt/benchmarks_2_jmh121"
    val benchs4Jmh121 = "ch/uzh/ifi/seal/bencher/analysis/finder/jdt/benchmarks_4_jmh121"
    val benchs4Jmh121v2 = "ch/uzh/ifi/seal/bencher/analysis/finder/jdt/benchmarks_4_jmh121_v2"

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

    object ObjectA {
        val fqn = "org.sample.stateObj.ObjectA"
    }

    object ObjectB {
        val fqn = "org.sample.stateObj.ObjectB"
    }
}