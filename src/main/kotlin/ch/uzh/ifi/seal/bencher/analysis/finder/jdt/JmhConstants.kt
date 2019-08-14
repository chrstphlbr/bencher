package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

object JMHConstants {
    object Annotation {
        const val benchmark = "org.openjdk.jmh.annotations.Benchmark"
        const val setup = "org.openjdk.jmh.annotations.Setup"
        const val tearDown = "org.openjdk.jmh.annotations.TearDown"
        const val fork = "org.openjdk.jmh.annotations.Fork"
        const val measurement = "org.openjdk.jmh.annotations.Measurement"
        const val warmup = "org.openjdk.jmh.annotations.Warmup"
        const val mode = "org.openjdk.jmh.annotations.BenchmarkMode"
        const val outputTimeUnit = "org.openjdk.jmh.annotations.OutputTimeUnit"
        const val param = "org.openjdk.jmh.annotations.Param"
        const val group = "org.openjdk.jmh.annotations.Group"
    }
}