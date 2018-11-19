package ch.uzh.ifi.seal.bencher.analysis

object JMHConstants {
    object Annotation {
        val benchmark = "Lorg/openjdk/jmh/annotations/Benchmark;"
        val setup = "Lorg/openjdk/jmh/annotations/Setup;"
        val tearDown = "Lorg/openjdk/jmh/annotations/TearDown;"
        val fork = "Lorg/openjdk/jmh/annotations/Fork;"
        val measurement = "Lorg/openjdk/jmh/annotations/Measurement;"
        val warmup = "Lorg/openjdk/jmh/annotations/Warmup;"
        val mode = "Lorg/openjdk/jmh/annotations/BenchmarkMode;"
        val outputTimeUnit = "Lorg/openjdk/jmh/annotations/OutputTimeUnit;"
    }
}
