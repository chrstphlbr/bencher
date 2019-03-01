//package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn;
//
//import java.io.*;
//import java.util.HashSet;
//import java.util.Set;
//
//public class CGHandler {
//
//    // singelton
//
//    private volatile static CGHandler instance;
//
//    public static CGHandler instance() {
//        if (instance == null) {
//            synchronized (CGHandler.class) {
//                if (instance == null) {
//                    instance = new CGHandler();
//                }
//            }
//        }
//        return instance;
//    }
//
//    // class
//
//    private static final String enc = "UTF-8";
//
//    private boolean trace;
//    private Set<String> entries;
//    private String entry;
//    private BufferedWriter w;
//
//    private CGHandler() {
//        entries = new HashSet<>();
//        trace = false;
//        entry = "";
//    }
//
//    public boolean startTracing(String entry, OutputStream os) {
//        synchronized (this) {
//            if (entries.contains(entry)) {
//                // already traced
//                return false;
//            }
//            trace = true;
//            this.entry = entry;
//            setUp(os);
//            return true;
//        }
//    }
//
//    private void setUp(OutputStream os) {
//        try {
//            w = new BufferedWriter(new OutputStreamWriter(os, enc));
//        } catch (UnsupportedEncodingException e) {
//            // should never happen
//            throw new IllegalStateException(e);
//        }
//    }
//
//    public boolean stopTracing(String entry) throws IOException {
//        synchronized (this) {
//            if (this.entry.equals(entry)) {
//                trace = false;
//                this.entry = "";
//                tearDown();
//                return true;
//            } else {
//                return false;
//            }
//        }
//    }
//
//    private void tearDown() throws IOException {
//        w.flush();
//        w.close();
//        w = null;
//    }
//
//    public void trace(String from, String to) throws IOException {
//        synchronized (this) {
//            if (!trace) {
//                return;
//            }
//            w.write(from + " -> " + to);
//            w.newLine();
//        }
//    }
//}
