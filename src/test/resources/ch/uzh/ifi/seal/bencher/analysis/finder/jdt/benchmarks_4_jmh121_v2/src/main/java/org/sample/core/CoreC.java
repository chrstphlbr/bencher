package org.sample.core;

public class CoreC {

    private String str;

    public CoreC(String str) {
        this.str = str;
    }

    public void m() {
        System.out.println("CoreC.m - " + str);
        System.out.println("Sdditional String CoreC");
    }
}
