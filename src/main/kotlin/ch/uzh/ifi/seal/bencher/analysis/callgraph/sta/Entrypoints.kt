package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.MF
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.analysis.JMHConstants
import ch.uzh.ifi.seal.bencher.analysis.byteCode
import ch.uzh.ifi.seal.bencher.analysis.finder.MethodFinder
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.classLoader.IMethod
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

// AllSubtypesApplicationEntryPoints inspired by implementation of https://bitbucket.org/delors/jcg/src/master/jcg_wala_testadapter/src/main/java/AllSubtypesOfApplicationEntrypoints.java
class AllApplicationEntrypoints(
        private val mf: MethodFinder<*>,
        packagePrefix: String? = null
) : EntrypointsGenerator {

    private val pkgPrefix: String? = packagePrefix?.byteCode()?.substring(1)

    override fun generate(scope: AnalysisScope, ch: ClassHierarchy): Either<String, Entrypoints> {
        val em = mf.all()
        if (em.isLeft()) {
            return Either.left(em.left().get())
        }
        val methods = em.right().get().toSet()

        val eps: LazyEntrypoints = ch.asSequence().mapNotNull { clazz ->
            if (clazz.isInterface || !isApplicationClass(scope, clazz) || !isLibraryClass(clazz, pkgPrefix)) {
                return@mapNotNull null
            }

            clazz.declaredMethods.asSequence().mapNotNull { m ->
                if (!m.isAbstract && (m.isPublic || m.isProtected)) {
                    val bm = m.bencherMethod()
                    val nm = findMethod(methods, bm)
                    if (nm != null) {
                        Pair(CGStartMethod(nm), DefaultEntrypoint(m, ch))
                    } else {
                        Pair(CGAdditionalMethod(bm), DefaultEntrypoint(m, ch))
                    }
                } else {
                    null
                }
            }
        }

        return Either.right(SingleCGEntrypoints().assemble(eps))
    }

    private fun findMethod(methods: Iterable<Method>, el: Method): Method? =
            methods.find {
                val isClass = it.clazz == el.clazz
                val isName = it.name == el.name
                val isParamsSize = it.params.size == el.params.size
                val isParamsType = it.params.asSequence()
                        .zip(el.params.asSequence())
                        .map { it.first == it.second }
                        .fold(true) { acc, n -> acc && n }
                val isParams = isParamsType && isParamsSize

                isClass && isName && isParams
            }

    // not used for now, because input from mf (MethodFinder) should always contain fully-qualified names
    private fun adaptedContains(methods: Iterable<Method>, el: Method): Boolean {
        val contains = methods.find {
            val isClass = it.clazz == el.clazz
            val isName = it.name == el.name
            val isParamsSize = it.params.size == el.params.size
            val isParamsType = it.params.asSequence()
                    .zip(el.params.asSequence())
                    .map { (p1, p2) ->
                        // true iff
                        // both types are the same (both fully qualified) or
                        // it (p1) is not fully qualified and bm (p2) without path is equal
                        p1 == p2 || p1 == p2.substringAfterLast(".")
                    }
                    .fold(true) { acc, n -> acc && n }
            val isParams = isParamsType || isParamsSize

            isClass && isName && isParams
        }
        return contains != null
    }
}

private fun isApplicationClass(scope: AnalysisScope, clazz: IClass): Boolean =
        scope.applicationLoader == clazz.classLoader.reference

private fun isLibraryClass(clazz: IClass, pkgPrefix: String?): Boolean =
        pkgPrefix == null || clazz.name.`package`.toString().startsWith(pkgPrefix)

class SingleCGEntrypoints : EntrypointsAssembler {
    override fun assemble(eps: LazyEntrypoints): Entrypoints =
            setOf(eps.fold(sequenceOf<Pair<CGMethod, Entrypoint>>()) { acc, s -> acc + s }.toSet())
}

class MultiCGEntrypoints : EntrypointsAssembler {
    override fun assemble(eps: LazyEntrypoints): Entrypoints =
            eps.map { it.toSet() }.toSet()
}

class BenchmarkWithSetupTearDownEntrypoints : MethodEntrypoints {
    override fun entrypoints(scope: AnalysisScope, ch: ClassHierarchy, m: Method): Either<String, Sequence<Pair<CGMethod, Entrypoint>>> {
        val className = m.clazz.byteCode()
        val tr = TypeReference.find(ClassLoaderReference.Application, className)
                ?: return Either.left("Could not get type reference for class $className")
        val c = ch.lookupClass(tr) ?: return Either.left("No class in class hierarchy for type $className")
        val mfm = c.allMethods.asSequence()
        val nsc = nestedStateEps(scope, ch, m)
        val epMethods = mfm + nsc

        return Either.right(
                epMethods.mapNotNull {
                    val dc = it.declaringClass
                    if (isApplicationClass(scope, dc)) {
                        DefaultEntrypoint(it, ch)
                    } else {
                        null
                    }
                }
                        .mapNotNull {
                            val method = it.method
                            if (m.name == method.name.toString()) {
                                Pair(CGStartMethod(m), it)
                            } else {
                                val bm = method.bencherMethod()
                                val epm: Method = if (method.isJMHSetup()) {
                                    MF.setupMethod(
                                            clazz = bm.clazz,
                                            name = bm.name,
                                            params = bm.params
                                    )
                                } else if (method.isJMHTearDown()) {
                                    MF.tearDownMethod(
                                            clazz = bm.clazz,
                                            name = bm.name,
                                            params = bm.params
                                    )
                                } else if (method.isInit || method.isClinit) {
                                    MF.plainMethod(
                                            clazz = bm.clazz,
                                            name = bm.name,
                                            params = bm.params
                                    )
                                } else {
                                    return@mapNotNull null
                                }
                                Pair(CGAdditionalMethod(epm), it)
                            }
                        }
        )
    }

    private fun nestedStateEps(scope: AnalysisScope, ch: ClassHierarchy, m: Method): Sequence<IMethod> {
        val className = m.clazz.byteCode()
        val params = m.params.map { it.byteCode() }
        return ch.asSequence().mapNotNull {
            val n = it.name.toUnicodeString()
            if (!params.contains(n)) {
                return@mapNotNull null
            }

            val stateClass = it.annotations.map { JMHConstants.Annotation.state.contains(it.type.name.toUnicodeString()) }.any { it }
            if (n.startsWith(className) && n != className && stateClass) {
                eps(it)
            } else {
                null
            }
        }.flatten()
    }

    private fun eps(c: IClass): Sequence<IMethod> =
            c.allMethods.asSequence().filter {
                it.isClinit || it.isInit || it.isJMHSetup() || it.isJMHTearDown()
            }
}
