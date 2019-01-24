package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.NoMethod
import ch.uzh.ifi.seal.bencher.analysis.*
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.bencherMethod
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.AnalysisScope
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.types.TypeName
import com.ibm.wala.types.TypeReference
import com.ibm.wala.util.config.AnalysisScopeReader
import org.apache.logging.log4j.LogManager
import org.funktionale.either.Either
import java.nio.file.Path


class IncompleteMethodFinder(
        private val methods: Iterable<Method>,
        private val jar: Path,
        private val unknownParameterNames: Set<String> = defaultUnknownParams
) : BencherWalaMethodFinder<Method> {

    private val classes = mutableMapOf<String, IClass>()

    // returns the corresponding (fully-qualified) methods found in jar
    // the returned list is in the same order as methods
    // if no corresponding method is found, NoMethod is at the same position as the not-found method in methods
    override fun all(): Either<String, List<Method>> {
        val parsed = parse()
        if (parsed.isLeft()) {
            return Either.left(parsed.left().get())
        }
        return Either.right(parsed.right().get().map { it.first })
    }

    override fun bencherWalaMethods(): Either<String, List<Pair<Method, IMethod?>>> = parse()

    private fun parse(): Either<String, List<Pair<Method, IMethod?>>> {
        val ef = WalaProperties.exclFile.fileResource()
        if (!ef.exists()) {
            return Either.left("Exclusions file '${WalaProperties.exclFile}' does not exist")
        }

        val scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jar.toAbsolutePath().toString(), ef)
        val ch = ClassHierarchyFactory.make(scope)

        return Either.right(methods.map { m ->
            val c = classForSourceCode(ch, scope, m.clazz, "method ($m)") ?: return@map Pair(NoMethod, null)
            val im = method(ch, scope, c, m)
            if (im == null) {
                Pair(NoMethod, null)
            } else {
                Pair(im.bencherMethod(), im)
            }
        })
    }

    private fun method(ch: ClassHierarchy, scope: AnalysisScope, c: IClass, m: Method): IMethod? {
        // filter the methods of c that have the same name as m and the same number of parameters
        val methods = c.declaredMethods.filter {
            // check if name matches
            if (it.name.toUnicodeString() != m.name) {
                return@filter false
            }

            // check if number of parameters match
            if (it.descriptor.numberOfParameters != m.params.size) {
                return@filter false
            }

            true
        }

        if (methods.isEmpty()) {
            log.debug("Class ${c.name.toUnicodeString()} does not contain a method with name '${m.name}' and ${m.params.size} parameters")
            return null
        }

        // find method with matching parameters (either with fqn-parameter match or parameter-class match)
        // or where an unknown parameter was passed
        val match = match(ch, scope, methods, m)
        if (match != null) {
            return match
        }

        return null
    }

    private fun match(ch: ClassHierarchy, scope: AnalysisScope, classMethods: Iterable<IMethod>, m: Method): IMethod? {
        val matches = classMethods.filter {
            if (it.descriptor.numberOfParameters == 0 && m.params.isEmpty()) {
                return@filter true
            } else if (it.descriptor.numberOfParameters != m.params.size) {
                return@filter false
            } else {
                // check if params match
                val paramMatch: Iterable<Boolean> = it.descriptor.parameters.mapIndexed { i, p ->
                    val pt = m.params[i]

                    if (isPrimitive(pt)) {
                        // check primitive parameter
                        val pf = checkParameter(ch, scope, p, pt)
                        if (pf) {
                            true
                        } else {
                            // check boxed type
                            val ppf = checkParameter(ch, scope, p, SourceCodeConstants.boxedType(pt)!!)
                            if (ppf) {
                                true
                            } else {
                                // check automatic widening conversions
                                val wide = SourceCodeConstants.wideningPrimitiveConversion(pt).find { primitive -> checkParameter(ch, scope, p, primitive) }
                                wide != null
                            }
                        }
                    } else {
                        val pf = checkParameter(ch, scope, p, pt)
                        if (pf) {
                            true
                        } else if (isBoxedPrimitive(pt, true)) {
                            // check unboxed type
                            checkParameter(ch, scope, p, SourceCodeConstants.unboxedType(pt, true)!!)
                        } else {
                            false
                        }
                    }
                }
                val paramsMatch = paramMatch.fold(true) { acc, n -> acc && n }
                paramsMatch
            }
        }

        if (matches.isEmpty()) {
            log.debug("No match for $m")
            return null
        } else if (matches.size > 1) {
            // ambiguous match
            log.debug("Ambiguous matches (${matches.size}): $matches")
            return null
        }
        val match = matches[0]
        val paramString = paramString(match.descriptor.parameters)
        log.debug("Match for $m and params ($paramString)")
        return match
    }

    private fun checkParameter(ch: ClassHierarchy, scope: AnalysisScope, p: TypeName, parameter: String): Boolean {
        val pc = classForParam(ch, scope, parameter, null)
        try {
            // fully-qualified names match
            if (p.toUnicodeString().sourceCode == parameter) {
                return true
            }

            // class names match
            if (p.className.toUnicodeString() == parameter) {
                //.substringAfterLast(".")
                return true
            }

            // check for subtype
            if (isSubtype(ch, scope, p, pc)) {
                return true
            }

            // unknown parameter
            if (unknownParameterNames.contains(parameter)) {
                return true
            }
        } finally {
            if (pc != null) {
                classes.putIfAbsent(parameter, pc)
            }
        }

        return false
    }

    private fun isSubtype(ch: ClassHierarchy, scope: AnalysisScope, p: TypeName, pc: IClass?): Boolean {
        // match if p is a supertype of parameter
        val cpc = classForParam(ch, scope, p.toUnicodeString(), null)
        if (cpc != null) {
            classes.putIfAbsent(p.toUnicodeString(), cpc)
            if (pc != null) {
                // has an IClass
                if (ch.isAssignableFrom(cpc, pc)) {
                    return true
                }
            }
        }
        return false
    }

    private fun classForParam(ch: ClassHierarchy, scope: AnalysisScope, sc: String, logForWhat: String?): IClass? {
        val c = classForSourceCode(ch, scope, sc, logForWhat)
        if (c != null) {
            return c
        }

        val chc = ch.find {
            val n = it.name
            if (n.toUnicodeString() == sc) {
                // fully-qualified names match
                true
            } else if (n.className.toUnicodeString() == sc) {
                // class names match
                true
            } else {
                false
            }
        }
        return chc
    }

    private fun classForSourceCode(ch: ClassHierarchy, scope: AnalysisScope, sc: String, logForWhat: String?): IClass? {
        if (classes.containsKey(sc)) {
            return classes[sc]
        }

        val classBc = sc.byteCode()

        val tr = TypeReference.find(scope.applicationLoader, classBc) ?: TypeReference.find(scope.primordialLoader, classBc)

        if (tr == null) {
            if (logForWhat != null) {
                log.debug("Could not get type reference for $logForWhat -> BC '$classBc'")
            }
            return null
        }

        val c = ch.lookupClass(tr)
        if (c == null) {
            if (logForWhat != null) {
                log.debug("Could not get IClass for TypeReference '$tr'")
            }
            return null
        }

        return c
    }

    private fun paramString(params: Array<TypeName>?): String {
        if (params == null) {
            return ""
        }
        return params.map { it.toUnicodeString().sourceCode }.joinToString(separator = ",")
    }

    companion object {
        val log = LogManager.getLogger(IncompleteMethodFinder::class.java.canonicalName)
        val defaultUnknownParams = setOf("unknown", "T", "R")
    }
}
