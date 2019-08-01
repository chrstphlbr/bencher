package org.sample.core;

public class CoreB implements CoreI {
    private String str;
    private CoreC c;

    public CoreB(String str, CoreC c) {
        this.str = str;
        this.c = c;
    }

    @Override
    public void m() {
        System.out.println("CoreB.m - " + str);
        c.m();
    }
}
