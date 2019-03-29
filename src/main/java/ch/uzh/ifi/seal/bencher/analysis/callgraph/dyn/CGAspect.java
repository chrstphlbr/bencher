//package ch.uzh.ifi.seal.bencher.analysis.callgraph.dyn;
//
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.Signature;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.aspectj.lang.reflect.MethodSignature;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//@Aspect
//public class CGAspect {
//    @Pointcut("@annotation(org.openjdk.jmh.annotations.Benchmark)")
//    void bench() {}
//
//    @Pointcut("execution(public * *.*(..))")
//    void exec() {}
//
//    @Pointcut("within(ch.uzh.ifi.seal.bencher..*)")
//    void lib() {}
//
//    @Around("bench()")
//    public Object benchAround(ProceedingJoinPoint jp) throws Throwable {
//        String bench = methodName(jp.getSignature());
//        CGHandler t = CGHandler.instance();
//        t.startTracing(bench, new FileOutputStream(new File("/Users/christophlaaber/tmp/jmh/aspectj/log4j2/cg_out/" + bench)));
//        //t.startTracing(bench, System.out);
//        Object o = jp.proceed();
//        t.stopTracing(bench);
//        return o;
//    }
//
//    @Before("call(* *.*(..)) && !lib()")
//    public void methodBefore(JoinPoint callee, JoinPoint.EnclosingStaticPart caller) throws IOException {
//        String from = methodName(caller.getSignature());
//        String to = methodName(callee.getSignature());
//        CGHandler.instance().trace(from, to);
//    }
//
//    private String methodName(Signature s) {
//        if (s instanceof MethodSignature) {
//            MethodSignature ms = ((MethodSignature) s);
//            StringBuilder sb = new StringBuilder(ms.getDeclaringTypeName() + "." + ms.getName());
//            sb.append("(");
//            Object[] params = ms.getParameterTypes();
//            boolean first = true;
//            for (int i = 0; i < params.length; i++) {
//                if (first) {
//                    first = false;
//                } else {
//                    sb.append(",");
//                }
//                sb.append(params[i].getClass().getCanonicalName());
//            }
//            sb.append(")");
//            return sb.toString();
//        }
//        return "not a method";
//    }
//}
