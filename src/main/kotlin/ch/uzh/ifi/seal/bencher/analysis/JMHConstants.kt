package ch.uzh.ifi.seal.bencher.analysis

object JMHConstants {
    object Annotation {
        const val benchmark = "Lorg/openjdk/jmh/annotations/Benchmark;"
        const val setup = "Lorg/openjdk/jmh/annotations/Setup;"
        const val tearDown = "Lorg/openjdk/jmh/annotations/TearDown;"
        const val fork = "Lorg/openjdk/jmh/annotations/Fork;"
        const val measurement = "Lorg/openjdk/jmh/annotations/Measurement;"
        const val warmup = "Lorg/openjdk/jmh/annotations/Warmup;"
        const val mode = "Lorg/openjdk/jmh/annotations/BenchmarkMode;"
        const val outputTimeUnit = "Lorg/openjdk/jmh/annotations/OutputTimeUnit;"
        const val state = "Lorg/openjdk/jmh/annotations/State;"
        const val group = "Lorg/openjdk/jmh/annotations/Group;"
    }
}
