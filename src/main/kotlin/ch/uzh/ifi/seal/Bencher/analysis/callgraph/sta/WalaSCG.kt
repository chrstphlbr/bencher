package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.Benchmark
import ch.uzh.ifi.seal.bencher.analysis.BenchmarkFinder
import ch.uzh.ifi.seal.bencher.analysis.callgraph.CGExecutor
import ch.uzh.ifi.seal.bencher.analysis.callgraph.WalaCGResult
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ipa.callgraph.Entrypoint
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.ipa.cha.IClassHierarchy
import com.ibm.wala.types.ClassLoaderReference
import com.ibm.wala.types.TypeReference
import com.ibm.wala.util.config.AnalysisScopeReader
import org.funktionale.either.Either
import java.io.File

class WalaSCG(
        private val jar: String,
        private val bf: BenchmarkFinder,
        private val algo: WalaSCGAlgo
) : CGExecutor<WalaCGResult> {

    override fun get(): Either<String, WalaCGResult> {
        val benchs = bf.all()
        if (benchs.isLeft()) {
            return Either.left(benchs.left().get())
        }

        val bs = benchs.right().get()

        val ef = File(this::class.java.classLoader.getResource(exclFile).file)
        if (!ef.exists()) {
            return Either.left("Exclusions file '${exclFile}' does not exist")
        }

        val scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jar, ef)
        val ch = ClassHierarchyFactory.make(scope)

        val eps = entryPoints(bs, ch)

        val opt = AnalysisOptions(scope, eps)
        opt.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL)

        val cache = AnalysisCacheImpl()
        val cg = algo.cg(opt, scope, cache, ch)

        return Either.right(transformCg(cg))
    }



    private fun entryPoints(benchs: Iterable<Benchmark>, ch: IClassHierarchy): Iterable<Entrypoint> =
            benchs.mapNotNull { entryPoint(ch, it.clazz, it.name) }

    private fun entryPoint(ch: IClassHierarchy, clazz: String, method: String): Entrypoint? {
        val c = ch.lookupClass(TypeReference.find(ClassLoaderReference.Application, bcClassName(clazz)))
        return c.allMethods.map {
            DefaultEntrypoint(it, ch)
        }.find {
            method == it.method.name.toString()
        }
    }

    private fun bcClassName(clazz: String): String = "L${clazz.replace(".", "/")}"

    private fun transformCg(cg: CallGraph): WalaCGResult {
        return WalaCGResult(
                toolCg = cg,
                cg = ch.uzh.ifi.seal.bencher.analysis.callgraph.CallGraph(
                        listOf()
                )
        )
    }

    companion object {
        val exclFile = "wala_exclusions.txt"
    }
}
