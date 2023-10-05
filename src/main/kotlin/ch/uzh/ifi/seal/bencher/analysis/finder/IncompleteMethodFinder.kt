package ch.uzh.ifi.seal.bencher.analysis.finder

import arrow.core.Either
import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.NoMethod
import ch.uzh.ifi.seal.bencher.analysis.*
import ch.uzh.ifi.seal.bencher.analysis.coverage.sta.bencherMethod
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.core.util.config.AnalysisScopeReader
import com.ibm.wala.ipa.callgraph.AnalysisScope
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.types.TypeName
import com.ibm.wala.types.TypeReference
import org.apache.logging.log4j.LogManager
import java.nio.file.Path


class IncompleteMethodFinder(
        private val methods: Iterable<Method>,
        private val jar: Path,
        private val acceptedAccessModifier: Set<AccessModifier> = AccessModifier.all,
        private val unknownParameterNames: Set<String> = defaultUnknownParams
) : BencherWalaMethodFinder<Method> {

    private val classes = mutableMapOf<String, IClass>()

    // returns the corresponding (fully-qualified) methods found in jar
    // the returned list is in the same order as methods
    // if no corresponding method is found, NoMethod is at the same position as the not-found method in methods
    override fun all(): Either<String, List<Method>> = parse().map { l -> l.map { it.first } }

    override fun bencherWalaMethods(): Either<String, List<Pair<Method, IMethod?>>> = parse()

    private fun parse(): Either<String, List<Pair<Method, IMethod?>>> {
        val ef = WalaProperties.exclFile.fileResource()
        if (!ef.exists()) {
            return Either.Left("Exclusions file '${WalaProperties.exclFile}' does not exist")
        }

        val scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(jar.toAbsolutePath().toString(), ef)
        val ch = ClassHierarchyFactory.make(scope)

        return Either.Right(methods.map { m ->
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
            // only consider accepted access modifiers
            if (!isAcceptedAccessModifier(it)) {
                return@filter false
            }

            // check if name matches or is initializer (constructor or static)
            if (it.name.toUnicodeString() != m.name ||
                    it.isInit && m.name != ByteCodeConstants.constructor ||
                    it.isClinit && m.name != ByteCodeConstants.staticInit
            ) {
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

    private fun isAcceptedAccessModifier(m: IMethod): Boolean =
            when {
                m.isPublic -> acceptedAccessModifier.contains(AccessModifier.PUBLIC)
                m.isProtected -> acceptedAccessModifier.contains(AccessModifier.PROTECTED)
                m.isPrivate -> acceptedAccessModifier.contains(AccessModifier.PRIVATE)
                else -> acceptedAccessModifier.contains(AccessModifier.PACKAGE)
            }

    private fun match(ch: ClassHierarchy, scope: AnalysisScope, classMethods: Iterable<IMethod>, m: Method): IMethod? {
        val matches: List<Pair<IMethod, Int>> = classMethods.mapNotNull {
            if (it.descriptor.numberOfParameters == 0 && m.params.isEmpty()) {
                return@mapNotNull Pair(it, 1)
            } else if (it.descriptor.numberOfParameters != m.params.size) {
                return@mapNotNull null
            } else {
                // check if params match
                val paramMatch: Iterable<Int> = it.descriptor.parameters.mapIndexed { i, p ->
                    val pt = m.params[i]

                    val matched = if (isPrimitive(pt)) {
                        // check primitive parameter
                        val pf = checkParameter(ch, scope, p, pt)
                        if (pf != 0) {
                            pf
                        } else {
                            // check boxed type
                            val ppf = checkParameter(ch, scope, p, SourceCodeConstants.boxedType(pt)!!)
                            if (ppf != 0) {
                                ppf
                            } else {
                                // check automatic widening conversions
                                val wide = SourceCodeConstants.wideningPrimitiveConversion(pt).mapNotNull { primitive ->
                                    val pppf = checkParameter(ch, scope, p, primitive)
                                    if (pppf == 0) {
                                        null
                                    } else {
                                        pppf
                                    }
                                }

                                if (wide.isEmpty()) {
                                    0
                                } else {
                                    wide.sorted().first()
                                }
                            }
                        }
                    } else {
                        val pf = checkParameter(ch, scope, p, pt)
                        if (pf != 0) {
                            pf
                        } else if (isBoxedPrimitive(pt, true)) {
                            // check unboxed type
                            checkParameter(ch, scope, p, SourceCodeConstants.unboxedType(pt, true)!!)
                        } else {
                            0
                        }
                    }
                    matched
                }
                val paramsMatch = paramMatch.fold(true) { acc, n -> acc && n != 0 }

                if (!paramsMatch) {
                    null
                } else {
                    Pair(it, paramMatch.fold(0) { acc, n -> acc + n })
                }
            }
        }

        return when {
            matches.isEmpty() -> {
                log.debug("No match for $m")
                null
            }
            matches.size > 1 -> {
                // ambiguous match
                val ambiguous = matches.sortedBy { it.second }.filter { it.second < 1000 }
                if (ambiguous.isEmpty()) {
                    log.debug("None chosen for ambiguous matches for $m (${matches.size}): ${matches.map { "${it.first} (weight: ${it.second})" }}")
                    null
                } else {
                    val chosen = ambiguous[0]
                    log.debug("Ambiguous matches for $m (${matches.size}): " +
                            "${matches.map { "${it.first} (weight: ${it.second})" }}" +
                            "  -> chosen: ${chosen.first} (weight: ${chosen.second})")
                    chosen.first
                }
            }
            else -> {
                val match = matches[0].first
                val paramString = paramString(match.descriptor.parameters)
                log.debug("Match for $m and params ($paramString)")
                match
            }
        }
    }

    // checkParameters returns
    // 0 if no match,
    // 1 if FQN match
    // 2 if only-class match
    // (# of hierarchy steps) if sub-type match
    // 1000 if unknown-parameter match
    private fun checkParameter(ch: ClassHierarchy, scope: AnalysisScope, p: TypeName, parameter: String): Int {
        val pc = classForParam(ch, scope, parameter, null)
        try {
            // fully-qualified names match
            if (p.toUnicodeString().sourceCode == parameter) {
                return 1
            }

            // class names match
            if (p.className.toUnicodeString() == parameter) {
                //.substringAfterLast(".")
                return 2
            }

            // check for subtype
            val st = isSubtype(ch, scope, p, pc)
            if (st > 0) {
                return st
            }

            // unknown parameter
            if (unknownParameterNames.contains(parameter)) {
                return 1000
            }
        } finally {
            if (pc != null) {
                classes.putIfAbsent(parameter, pc)
            }
        }

        return 0
    }

    private fun isSubtype(ch: ClassHierarchy, scope: AnalysisScope, cp: TypeName, pc: IClass?): Int {
        if (pc == null) {
            return 0
        }

        // get IClass for parameter of declared method of class
        // return false if could not extract IClass for the parameter
        val cpc = classForParam(ch, scope, cp.toUnicodeString(), null) ?: return 0
        classes.putIfAbsent(cp.toUnicodeString(), cpc)

        if (!ch.isAssignableFrom(cpc, pc)) {
            return 0
        }

        val depth = when {
            // if parameter of method candidate and parameter to match are interfaces
            cpc.isInterface && pc.isInterface -> interfaceDepth(ch, cpc, pc, 0)
            // if parameter of method candidate is an Interface and parameter to match is a class
            cpc.isInterface -> callSiteInterfaceDepth(ch, cpc, pc, 0)
            // parameter of method candidate and parameter to match are classes
            else -> classDepth(ch, cpc, pc, 0)
        }

        return if (depth == -1) {
            0
        } else {
            depth + 1
        }
    }

    private fun interfaceDepth(ch: ClassHierarchy, cpc: IClass, pc: IClass, level: Int): Int {
        if (!cpc.isInterface || !pc.isInterface) {
            return -1
        }

        if (cpc == pc) {
            return level
        }

        val depths = pc.directInterfaces.map { interfaceDepth(ch, cpc, it, level + 1) }.filter { it != -1 }
        return if (depths.isEmpty()) {
            return -1
        } else {
            depths.sorted().first()
        }
    }

    private fun classDepth(ch: ClassHierarchy, cpc: IClass, pc: IClass, level: Int): Int {
        if (cpc == pc) {
            return level
        }

        return if (pc.superclass == null) {
            -1
        } else {
            classDepth(ch, cpc, pc.superclass, level + 1)
        }
    }

    private fun callSiteInterfaceDepth(ch: ClassHierarchy, cpc: IClass, pc: IClass?, level: Int): Int {
        if (pc == null) {
            return -1
        }

        val depths = pc.directInterfaces.map { interfaceDepth(ch, cpc, it, level + 1) }.filter { it != -1 }
        val classDepth = callSiteInterfaceDepth(ch, cpc, pc.superclass, level + 1)

        return if (depths.isEmpty()) {
            classDepth
        } else {
            val interfaceDepth = depths.sorted().first()
            if (classDepth == -1) {
                interfaceDepth
            } else if (classDepth < interfaceDepth) {
                classDepth
            } else {
                interfaceDepth
            }
        }
    }

    private fun classForParam(ch: ClassHierarchy, scope: AnalysisScope, sc: String, logForWhat: String?): IClass? {
        val c = classForSourceCode(ch, scope, sc, logForWhat)
        if (c != null) {
            return c
        }

        return ch.find {
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
    }

    private fun classForSourceCode(ch: ClassHierarchy, scope: AnalysisScope, sc: String, logForWhat: String?): IClass? {
        if (classes.containsKey(sc)) {
            return classes[sc]
        }

        val classBc = sc.byteCode()

        val tr = TypeReference.find(scope.applicationLoader, classBc)
                ?: TypeReference.find(scope.primordialLoader, classBc)

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
        val defaultUnknownParams = setOf("unknown")
        private const val unknownMatch: Int = 1000
    }
}
