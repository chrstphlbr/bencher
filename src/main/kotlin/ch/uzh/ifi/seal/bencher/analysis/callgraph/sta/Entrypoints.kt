package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import ch.uzh.ifi.seal.bencher.analysis.byteCode
import ch.uzh.ifi.seal.bencher.analysis.finder.MethodFinder
import com.ibm.wala.ipa.callgraph.AnalysisScope
import com.ibm.wala.ipa.callgraph.Entrypoint
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.types.ClassLoaderReference
import com.ibm.wala.types.TypeReference
import org.funktionale.either.Either


// for each CG construction a list of methd-entrypoints-pairs
typealias Entrypoints = Iterable<Iterable<Pair<CGMethod, Entrypoint>>>

private typealias LazyEntrypoints = Sequence<Sequence<Pair<CGMethod, Entrypoint>>>

sealed class CGMethod(
        open val method: Method
)

data class CGStartMethod(
        override val method: Method
) : CGMethod(method)

data class CGAdditionalMethod(
        override val method: Method
) : CGMethod(method)

interface EntrypointsGenerator {
    fun generate(scope: AnalysisScope, ch: ClassHierarchy): Either<String, Entrypoints>
}

interface MethodEntrypoints {
    fun entrypoints(scope: AnalysisScope, ch: ClassHierarchy, m: Method): Either<String, Sequence<Pair<CGMethod, Entrypoint>>>
}

interface EntrypointsAssembler {
    fun assemble(eps: LazyEntrypoints): Entrypoints
}

class CGEntrypoints(
        private val mf: MethodFinder<*>,
        private val me: MethodEntrypoints,
        private val ea: EntrypointsAssembler
) : EntrypointsGenerator {

    override fun generate(scope: AnalysisScope, ch: ClassHierarchy): Either<String, Entrypoints> {
        val ems = mf.all()
        if (ems.isLeft()) {
            return Either.left(ems.left().get())
        }

        val ms = ems.right().get()

        val cgEps: LazyEntrypoints = ms.asSequence().mapNotNull { m ->
            val eps = me.entrypoints(scope, ch, m)
            if (eps.isLeft()) {
                null
            } else {
                eps.right().get()
            }
        }

        return Either.right(ea.assemble(cgEps))
    }
}

class SingleCGEntrypoints : EntrypointsAssembler {
    override fun assemble(eps: LazyEntrypoints): Entrypoints =
            listOf(eps.fold(sequenceOf<Pair<CGMethod, Entrypoint>>()) { acc, s -> acc + s }.toList())
}

class MultiCGEntrypoints : EntrypointsAssembler {
    override fun assemble(eps: LazyEntrypoints): Entrypoints =
            eps.map { it.toList() }.toList()
}

class BenchmarkWithSetupTearDownEntrypoints : MethodEntrypoints {
    override fun entrypoints(scope: AnalysisScope, ch: ClassHierarchy, m: Method): Either<String, Sequence<Pair<CGMethod, Entrypoint>>> {
        val className = m.clazz.byteCode
        val tr = TypeReference.find(ClassLoaderReference.Application, className) ?: return Either.left("Could not get type reference for class $className")
        val c = ch.lookupClass(tr) ?: return Either.left("No class in class hierarchy for type $className")
        return Either.right(c.allMethods.asSequence().map {
            DefaultEntrypoint(it, ch)
        }.mapNotNull {
            val method = it.method
            if (m.name == method.name.toString()) {
                Pair(CGStartMethod(m), it)
            } else if (method.isJMHSetup()) {
                val pm = method.bencherMethod()
                Pair(CGAdditionalMethod(SetupMethod(
                        clazz = pm.clazz,
                        name = pm.name,
                        params = pm.params
                )), it)
            } else if (method.isJMHTearDown()) {
                val pm = method.bencherMethod()
                Pair(CGAdditionalMethod(TearDownMethod(
                        clazz = pm.clazz,
                        name = pm.name,
                        params = pm.params
                )), it)
            } else {
                null
            }
        })
    }
}

// AllSubtypesApplicationEntryPoints inspired by implementation of https://bitbucket.org/delors/jcg/src/master/jcg_wala_testadapter/src/main/java/AllSubtypesOfApplicationEntrypoints.java
//class AllSubtypesApplicationEntrypoints : MethodEntrypoints {
//    override fun entrypoints(scope: AnalysisScope, ch: ClassHierarchy, m: Method): Either<String, Sequence<Pair<Method, Entrypoint>>> {
//
//        Either.right()
//    }
//
//    private fun createEntrypoints(scope: AnalysisScope, ch: ClassHierarchy): Entrypoint =
//            ch.mapNotNull { clazz ->
//                if (clazz.isInterface || !isApplicationClass(scope, clazz)) {
//                    return@mapNotNull null
//                }
//
//                clazz.declaredMethods.mapNotNull { m ->
//                    if (!m.isAbstract) {
//                        SubtypesEntrypoint(m, ch)
//                    } else {
//                        null
//                    }
//                }
//            }
//
//    private fun isApplicationClass(scope: AnalysisScope, clazz: IClass): Boolean {
//        return scope.applicationLoader == clazz.classLoader.reference
//    }
//}
