package org.sample.core;

public class CoreA implements CoreI {
    private CoreI i;
    private String str;
    private String additionalString = "Additional String CoreA";

    public CoreA(String str, CoreI i) {
        this.str = str;
        this.i = i;
    }

    @Override
    public void m() {
        System.out.println("CoreA.m - " + str);
        i.m();
        System.out.println(this.additionalString);
    }
}
