package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

object JMHConstants {
    object Annotation {
        val benchmark = "org.openjdk.jmh.annotations.Benchmark"
        val setup = "org.openjdk.jmh.annotations.Setup"
        val tearDown = "org.openjdk.jmh.annotations.TearDown"
        val fork = "org.openjdk.jmh.annotations.Fork"
        val measurement = "org.openjdk.jmh.annotations.Measurement"
        val warmup = "org.openjdk.jmh.annotations.Warmup"
        val mode = "org.openjdk.jmh.annotations.BenchmarkMode"
        val outputTimeUnit = "org.openjdk.jmh.annotations.OutputTimeUnit"
        val state = "org.openjdk.jmh.annotations.State"
    }
}