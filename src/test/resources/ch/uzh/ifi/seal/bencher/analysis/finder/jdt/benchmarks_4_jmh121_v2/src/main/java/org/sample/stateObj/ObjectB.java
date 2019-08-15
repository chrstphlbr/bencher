package org.sample.stateObj;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class ObjectB {

    @Param({"3", "4"})
    public String str1 = "str";

    @Param({"1"})
    public String str3 = "str";
}
