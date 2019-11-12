package org.sample;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Fork(value = 2, warmups = 2)
@Measurement(iterations = 10, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
@Warmup(iterations = 10, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class NestedBenchmark {

    @Fork(10)
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public static class Bench1 {
        @Warmup(iterations = 20, time = 100, timeUnit = TimeUnit.MILLISECONDS)
        @Measurement(iterations = 10)
        @Benchmark
        public void bench11() {
            System.out.println("bench11");
        }

        @Benchmark
        public void bench12() {
            System.out.println("bench12");
        }
    }

    @Fork(5)
    @Measurement(iterations = 50, time = 500, timeUnit = TimeUnit.MILLISECONDS)
    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    public void bench2() {
        System.out.println("bench2");
    }

    @Warmup(iterations = 2, timeUnit = TimeUnit.MILLISECONDS, time = 100)
    public static class Bench3 {
        @Fork(warmups = 10)
        @Benchmark
        @BenchmarkMode({Mode.Throughput, Mode.SampleTime})
        @OutputTimeUnit(TimeUnit.MICROSECONDS)
        public void bench31() {
            System.out.println("bench31");
        }

        public static class Bench32 {
            @Benchmark
            @BenchmarkMode(Mode.SingleShotTime)
            public void bench321() {
                System.out.println("bench321");
            }
        }
    }
}
