package org.sample;

import org.openjdk.jmh.annotations.*;
import org.sample.core.CoreB;
import org.sample.core.CoreC;

@State(Scope.Benchmark)
public class BenchsWithGroup {

    private String str = "str";
    private CoreB b;

    @Setup
    public void setup() {
        b = new CoreB(str, new CoreC(str));
    }

    @Benchmark
    @Group("groupName")
    @Fork(5)
    public void bench1() {
        b.m();
    }

    @Benchmark
    @Group("groupName")
    public void bench2() {
        b.m();
    }

    @Benchmark
    public void bench3() {
        b.m();
    }
}
