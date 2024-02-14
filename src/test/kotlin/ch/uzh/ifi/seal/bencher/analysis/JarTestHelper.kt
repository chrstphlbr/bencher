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

    val javaSettings = JavaSettings(
        home = null,
        jvmArgs = null,
    )

    interface HasFile {
        val file: String
    }

    object BenchNonParameterized : HasFile {
        val fqn = "org.sample.BenchNonParameterized"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench2 = Benchmark(clazz = fqn, name = "bench2", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
        override val file = "BenchNonParameterized.java"
    }

    object BenchParameterized : HasFile {
        val fqn = "org.sample.BenchParameterized"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench1 = Benchmark(clazz = fqn, name = "bench1", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf(Pair("str", "1"), Pair("str", "2"), Pair("str", "3")))
        val setup = SetupMethod(clazz = fqn, name = "setup", params = listOf(), returnType = SourceCodeConstants.void)
        override val file = "BenchParameterized.java"
    }

    object BenchParameterized2 : HasFile {
        val fqn = "org.sample.BenchParameterized2"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench4 = Benchmark(clazz = fqn, name = "bench4", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf(Pair("str", "1"), Pair("str", "2"), Pair("str", "3")))
        val setup = SetupMethod(clazz = fqn, name = "setup", params = listOf(), returnType = SourceCodeConstants.void)
        override val file = "BenchParameterized2.java"
    }

    object BenchParameterized2v2 : HasFile {
        val fqn = "org.sample.BenchParameterized2"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench4 = Benchmark(clazz = fqn, name = "bench4", params = listOf("org.openjdk.jmh.infra.Blackhole"), returnType = SourceCodeConstants.void, jmhParams = listOf(Pair("str", "1"), Pair("str", "2"), Pair("str", "3"), Pair("str2", "1"), Pair("str2", "2"), Pair("str2", "3")))
        val setup = SetupMethod(clazz = fqn, name = "setup", params = listOf(), returnType = SourceCodeConstants.void)
        override val file = "BenchParameterized2.java"
    }

    object OtherBench : HasFile {
        val fqn = "org.sample.OtherBench"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench3 = Benchmark(clazz = fqn, name = "bench3", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
        val setup = SetupMethod(clazz = fqn, name = "setup", params = listOf(), returnType = SourceCodeConstants.void)
        val tearDown = TearDownMethod(clazz = fqn, name = "teardown", params = listOf(), returnType = SourceCodeConstants.void)
        override val file = "OtherBench.java"
    }

    object NestedBenchmark : HasFile {
        val fqn = "org.sample.NestedBenchmark"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench2 = Benchmark(clazz = fqn, name = "bench2", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
        override val file = "NestedBenchmark.java"

        object Bench1 : HasFile {
            val fqn = NestedBenchmark.fqn + "\$Bench1"
            val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
            val bench11 = Benchmark(clazz = fqn, name = "bench11", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
            val bench12 = Benchmark(clazz = fqn, name = "bench12", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
            override val file = NestedBenchmark.file
        }

        object Bench3 : HasFile {
            val fqn = NestedBenchmark.fqn + "\$Bench3"
            val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
            val bench31 = Benchmark(clazz = fqn, name = "bench31", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
            override val file = NestedBenchmark.file

            object Bench32 : HasFile {
                val fqn = Bench3.fqn + "\$Bench32"
                val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
                val bench321 = Benchmark(clazz = fqn, name = "bench321", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
                override val file = NestedBenchmark.file
            }
        }
    }

    object BenchsWithGroup : HasFile {
        val fqn = "org.sample.BenchsWithGroup"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench1 = Benchmark(clazz = fqn, name = "bench1", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf(), group = "groupName")
        val bench2 = Benchmark(clazz = fqn, name = "bench2", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf(), group = "groupName")
        val bench3 = Benchmark(clazz = fqn, name = "bench3", params = listOf(), returnType = SourceCodeConstants.void, jmhParams = listOf())
        val setup = SetupMethod(clazz = fqn, name = "setup", params = listOf(), returnType = SourceCodeConstants.void)
        override val file = "BenchsWithGroup.java"
    }

    object BenchsStateObj : HasFile {
        val fqn = "org.sample.BenchsWithStateObj"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val bench1 = Benchmark(clazz = fqn, name = "bench1", params = listOf("org.sample.stateObj.ObjectA"), returnType = SourceCodeConstants.void, jmhParams = listOf(Pair("str1", "1"), Pair("str1", "2"), Pair("str2", "1"), Pair("str4", "5")))
        val bench2 = Benchmark(clazz = fqn, name = "bench2", params = listOf("org.sample.stateObj.ObjectA", "org.sample.stateObj.ObjectB"), returnType = SourceCodeConstants.void, jmhParams = listOf(Pair("str1", "1"), Pair("str1", "2"), Pair("str2", "1"), Pair("str3", "1"), Pair("str4", "5")))
        val bench3 = Benchmark(clazz = fqn, name = "bench3", params = listOf("org.sample.stateObj.ObjectB", "org.sample.stateObj.ObjectA"), returnType = SourceCodeConstants.void, jmhParams = listOf(Pair("str1", "3"), Pair("str1", "4"), Pair("str3", "1"), Pair("str2", "1"), Pair("str4", "5")))
        override val file = "BenchsWithStateObj.java"
    }

    object CoreI : HasFile {
        val fqn = "org.sample.core.CoreI"
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf(), returnType = SourceCodeConstants.void)
        override val file = "CoreI.java"
    }

    object CoreA : HasFile {
        val fqn = "org.sample.core.CoreA"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf("java.lang.String", "org.sample.core.CoreI"), returnType = SourceCodeConstants.void)
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf(), returnType = SourceCodeConstants.void)
        override val file = "CoreA.java"
    }

    object CoreB : HasFile {
        val fqn = "org.sample.core.CoreB"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf("java.lang.String", "org.sample.core.CoreC"), returnType = SourceCodeConstants.void)
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf(), returnType = SourceCodeConstants.void)
        override val file = "CoreB.java"
    }

    object CoreC : HasFile {
        val fqn = "org.sample.core.CoreC"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf("java.lang.String"), returnType = SourceCodeConstants.void)
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf(), returnType = SourceCodeConstants.void)
        override val file = "CoreC.java"
    }

    object CoreD : HasFile {
        val fqn = "org.sample.core.CoreD"
        val constructor = PlainMethod(clazz = fqn, name = "<init>", params = listOf(), returnType = SourceCodeConstants.void)
        val m = PlainMethod(clazz = fqn, name = "m", params = listOf(), returnType = SourceCodeConstants.void)
        override val file = "CoreD.java"
    }

    object CoreE : HasFile {
        val fqn = "org.sample.core.CoreE"
        val mn_1 = PlainMethod(clazz = fqn, name = "mn", params = listOf(), returnType = SourceCodeConstants.void)
        val mn_2 = PlainMethod(clazz = fqn, name = "mn", params = listOf("java.lang.String", "java.lang.String[]"), returnType = SourceCodeConstants.void)
        val mn_3 = PlainMethod(clazz = fqn, name = "mn", params = listOf("int", "java.lang.String"), returnType = SourceCodeConstants.void)
        val mn1_1 = PlainMethod(clazz = fqn, name = "mn1", params = listOf("java.lang.String", "java.lang.String[]"), returnType = SourceCodeConstants.void)
        val mn1_2 = PlainMethod(clazz = fqn, name = "mn1", params = listOf("java.lang.String", "java.util.List"), returnType = SourceCodeConstants.void)
        val mn2 = PlainMethod(clazz = fqn, name = "mn2", params = listOf("int", "java.lang.String", "java.lang.String[]"), returnType = SourceCodeConstants.void)
        override val file = "CoreE.java"
    }

    object ObjectA : HasFile {
        val fqn = "org.sample.stateObj.ObjectA"
        override val file = "ObjectA.java"
    }

    object ObjectB : HasFile {
        val fqn = "org.sample.stateObj.ObjectB"
        override val file = "ObjectB.java"
    }
}
