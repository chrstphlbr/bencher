package ch.uzh.ifi.seal.bencher.analysis.callgraph.sta

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.SetupMethod
import ch.uzh.ifi.seal.bencher.TearDownMethod
import ch.uzh.ifi.seal.bencher.analysis.byteCode
import ch.uzh.ifi.seal.bencher.analysis.finder.MethodFinder
import com.ibm.wala.ipa.callgraph.Entrypoint
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.types.ClassLoaderReference
import com.ibm.wala.types.TypeReference
import org.funktionale.either.Either

// for each CG construction a list of methd-entrypoints-pairs
typealias Entrypoints = Iterable<Iterable<Pair<Method, Entrypoint>>>

private typealias LazyEntrypoints = Sequence<Sequence<Pair<Method, Entrypoint>>>

interface EntrypointsGenerator {
    fun generate(ch: ClassHierarchy): Either<String, Entrypoints>
}

interface MethodEntrypoints {
    fun entrypoints(ch: ClassHierarchy, m: Method): Sequence<Pair<Method, Entrypoint>>
}

interface EntrypointsAssembler {
    fun assemble(eps: LazyEntrypoints): Entrypoints
}

class CGEntrypoints(
        private val mf: MethodFinder<*>,
        private val me: MethodEntrypoints,
        private val ea: EntrypointsAssembler
) : EntrypointsGenerator {

    override fun generate(ch: ClassHierarchy): Either<String, Entrypoints> {
        val ems = mf.all()
        if (ems.isLeft()) {
            return Either.left(ems.left().get())
        }

        val ms = ems.right().get()

        val cgEps: LazyEntrypoints = ms.asSequence().map { m ->
            me.entrypoints(ch, m)
        }

        return Either.right(ea.assemble(cgEps))
    }
}

class SingleCGEntrypoints : EntrypointsAssembler {
    override fun assemble(eps: LazyEntrypoints): Entrypoints =
            listOf(eps.fold(sequenceOf<Pair<Method, Entrypoint>>(), { acc, s -> acc + s }).toList())
}

class MultiCGEntrypoints : EntrypointsAssembler {
    override fun assemble(eps: LazyEntrypoints): Entrypoints =
            eps.map { it.toList() }.toList()
}

class BenchmarkWithSetupTearDownEntrypoints : MethodEntrypoints {
    override fun entrypoints(ch: ClassHierarchy, m: Method): Sequence<Pair<Method, Entrypoint>> {
        val c = ch.lookupClass(TypeReference.find(ClassLoaderReference.Application, m.clazz.byteCode))
        return c.allMethods.asSequence().map {
            DefaultEntrypoint(it, ch)
        }.mapNotNull {
            val method = it.method
            if (m.name == method.name.toString()) {
                Pair(m, it)
            } else if (method.isJMHSetup()) {
                val pm = method.bencherMethod()
                Pair(SetupMethod(
                        clazz = pm.clazz,
                        name = pm.name,
                        params = pm.params
                ), it)
            } else if (method.isJMHTearDown()) {
                val pm = method.bencherMethod()
                Pair(TearDownMethod(
                        clazz = pm.clazz,
                        name = pm.name,
                        params = pm.params
                ), it)
            } else {
                null
            }
        }
    }
}
