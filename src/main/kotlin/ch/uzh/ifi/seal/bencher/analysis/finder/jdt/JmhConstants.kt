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
        const val state = "org.openjdk.jmh.annotations.State"
        const val group = "org.openjdk.jmh.annotations.Group"
    }

    object Package {
        const val annotation = "org.openjdk.jmh.annotations"
        const val infra = "org.openjdk.jmh.infra"
    }

    object Class {
        const val blackhole = "org.openjdk.jmh.infra.Blackhole"
        const val control = "org.openjdk.jmh.infra.Control"
        const val benchmarkParams = "org.openjdk.jmh.infra.BenchmarkParams"
        const val iterationParams = "org.openjdk.jmh.infra.IterationParams"
        const val threadParams = "org.openjdk.jmh.infra.ThreadParams"
    }
}