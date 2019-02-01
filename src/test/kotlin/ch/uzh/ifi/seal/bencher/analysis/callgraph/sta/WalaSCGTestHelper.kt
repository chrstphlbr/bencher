package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.*
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGResult
import ch.uzh.ifi.seal.bencher.analysis.callgraph.MethodCall
import ch.uzh.ifi.seal.bencher.analysis.callgraph.SimpleCGPrinter
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.util.config.AnalysisScopeReader
import org.junit.jupiter.api.Assertions
import java.io.File

object WalaSCGTestHelper {

    val exclusionsFile = "wala_exclusions.txt".fileResource()

    fun reachable(cg: CGResult, from: Method, to: Method, level: Int) = reachable(cg, from, MethodCall(to, level))

    fun reachable(cg: CGResult, from: Method, to: MethodCall) {
        val benchCalls = cg.calls.get(from)
        if (benchCalls == null) {
            Assertions.fail<String>("No benchmark for $from")
            return
        }
        val call = benchCalls.find { it == to}
        Assertions.assertNotNull(call, "No method call ($to) from bench ($from) reachable")
    }

    fun plainMethodCall(m: Method, level: Int): MethodCall =
            MethodCall(
                    method = PlainMethod(
                            clazz = m.clazz,
                            name = m.name,
                            params = m.params
                    ),
                    level = level
            )

    fun possibleMethodCall(m: Method, level: Int, nrPossibleTargets: Int, idPossibleTargets: Int): MethodCall =
            MethodCall(
                    method = PossibleMethod(
                            clazz = m.clazz,
                            name = m.name,
                            params = m.params,
                            idPossibleTargets = idPossibleTargets,
                            nrPossibleTargets = nrPossibleTargets
                    ),
                    level = level
            )

    fun assertCGResult(wcg: WalaSCG, jar: File): CGResult {
        val cgRes = wcg.get(jar.toPath())
        if (cgRes.isLeft()) {
            Assertions.fail<String>("Could not get CG: ${cgRes.left().get()}")
        }

        return cgRes.right().get()
    }

    fun errStr(call: String, level: Int): String =
            "call to $call on level $level not found"

    fun cha(jar: String): ClassHierarchy {
        val ef = WalaSCGTestHelper.exclusionsFile
        Assertions.assertTrue(ef.exists(), "Wala-test-exclusions file does not exist")
        val jarFile = jar.fileResource()
        Assertions.assertTrue(jarFile.exists(), "Jar file ($jar) does not exist")
        val jarPath = jarFile.absolutePath

        val scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jarPath, ef)
        return ClassHierarchyFactory.make(scope)
    }

    fun print(cg: CGResult) {
        val p = SimpleCGPrinter(System.out)
        p.print(cg)
    }
}
