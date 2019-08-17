package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.Annotation

object FullyQualifiedNameHelper {
    private const val ANNOTATION_PACKAGE = JMHConstants.Package.annotation
    private const val INFRA_PACKAGE = JMHConstants.Package.infra
    private const val BLACKHOLE = JMHConstants.Class.blackhole

    /*
        Returns the fully qualified name of the annotation if its a jmh annotation
     */
    fun get(node: Annotation): String {
        if (node.typeName.isQualifiedName) {
            return node.typeName.fullyQualifiedName
        } else {
            if (!checkIfJmhAnnotation(node, node.typeName.fullyQualifiedName)) {
                return node.typeName.fullyQualifiedName
            } else {
                return ANNOTATION_PACKAGE + "." + node.typeName.fullyQualifiedName
            }
        }
    }

    fun getClassName(node: TypeDeclaration): String {
        var fullyQualifiedName = ""
        var iterator: ASTNode? = node

        while (iterator != null) {
            when (iterator) {
                is TypeDeclaration -> fullyQualifiedName = ".${iterator.name.identifier}$fullyQualifiedName"
                is CompilationUnit -> {
                    if (iterator.`package` == null) {
                        // its in the default package
                        fullyQualifiedName = node.name.identifier
                    } else {
                        fullyQualifiedName = iterator.`package`.name.fullyQualifiedName + fullyQualifiedName
                    }
                }
            }

            iterator = iterator.parent
        }

        return fullyQualifiedName
    }

    private fun checkIfJmhAnnotation(node: ASTNode, typeName: String): Boolean {
        var iterator: ASTNode? = node

        while (iterator != null) {
            if (iterator is CompilationUnit) {
                iterator.imports().forEach {
                    if (it is ImportDeclaration) {
                        val exact = it.name.fullyQualifiedName.contains("$ANNOTATION_PACKAGE.$typeName") && !it.isOnDemand
                        val star = it.name.fullyQualifiedName.contains(ANNOTATION_PACKAGE) && it.isOnDemand

                        if (exact || star) {
                            return true
                        }
                    }
                }
            }

            iterator = iterator.parent
        }

        return false
    }

    fun checkIfBlackhole(type: ASTNode): Boolean {
        if (type.toString().endsWith("Blackhole")) {

            var iterator: ASTNode? = type

            while (iterator != null) {
                if (iterator is CompilationUnit) {
                    iterator.imports().forEach {
                        if (it is ImportDeclaration) {
                            val exact = it.name.fullyQualifiedName.contains("$BLACKHOLE") && !it.isOnDemand
                            val star = it.name.fullyQualifiedName.contains(INFRA_PACKAGE) && it.isOnDemand

                            if (exact || star) {
                                return true
                            }
                        }
                    }
                }

                iterator = iterator.parent
            }
        }

        return false
    }
}