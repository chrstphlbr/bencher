package ch.uzh.ifi.seal.bencher.analysis.finder

import ch.uzh.ifi.seal.bencher.Method
import ch.uzh.ifi.seal.bencher.NoMethod
import ch.uzh.ifi.seal.bencher.analysis.WalaProperties
import ch.uzh.ifi.seal.bencher.analysis.byteCode
import ch.uzh.ifi.seal.bencher.analysis.callgraph.sta.bencherMethod
import ch.uzh.ifi.seal.bencher.analysis.sourceCode
import ch.uzh.ifi.seal.bencher.fileResource
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.classLoader.IMethod
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
) : MethodFinder<Method> {
    // returns the corresponding (fully-qualified) methods found in jar
    // the returned list is in the same order as methods
    // if no corresponding method is found, NoMethod is at the same position as the not-found method in methods
    override fun all(): Either<String, List<Method>> {
        val ef = WalaProperties.exclFile.fileResource()
        if (!ef.exists()) {
            return Either.left("Exclusions file '${WalaProperties.exclFile}' does not exist")
        }

        val scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jar.toAbsolutePath().toString(), ef)
        val ch = ClassHierarchyFactory.make(scope)

        return Either.right(methods.map { m ->
            val classBc = m.clazz.byteCode
            val tr = TypeReference.find(scope.applicationLoader, classBc)
            if (tr == null) {
                log.debug("Could not get type reference for method ($m) -> BC '$classBc'")
                return@map NoMethod
            }

            val c = ch.lookupClass(tr)
            if (c == null) {
                log.debug("Could not get IClass for TypeReference '$tr'")
                return@map NoMethod
            }

            method(c, m)
        })
    }

    private fun method(c: IClass, m: Method): Method {
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
            log.debug("Class ${c.name.toUnicodeString()} does not contain a method with name '${m.name} and ${m.params.size} parameters")
            return NoMethod
        }

        // find method with matching parameters (either with fqn-parameter match or parameter-class match)
        // or where an unknown parameter was passed
        val match = match(methods, m)
        if (match != null) {
            return match
        }

        log.debug("No method found for $m")
        return NoMethod
    }

    private fun match(classMethods: Iterable<IMethod>, m: Method): Method? {
            val matches = classMethods.filter {
                if (it.descriptor.numberOfParameters == 0 && m.params.isEmpty()) {
                    return@filter true
                } else if (it.descriptor.numberOfParameters != m.params.size) {
                    return@filter false
                } else {
                    // check if params either completely match (both fully qualified) or class name matches
                    val paramMatch: Iterable<Boolean> = it.descriptor.parameters.mapIndexed { i, p ->
                        val pt = m.params[i]
                        // fully-qualified names match
                        if (p.toUnicodeString().sourceCode == pt) {
                            return@mapIndexed true
                        }

                        // class names match
                        if (p.className.toUnicodeString() == pt.substringAfterLast(".")) {
                            return@mapIndexed true
                        }

                        // unknown parameter
                        if (unknownParameterNames.contains(pt)) {
                            return@mapIndexed true
                        }

                        false
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
        return match.bencherMethod()
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
    }
}
