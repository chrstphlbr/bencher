package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import org.eclipse.jdt.core.dom.*
import org.eclipse.jdt.core.dom.Annotation

object FullyQualifiedNameHelper {
    /*
        Returns the fully qualified name of the annotation if its a jmh annotation
     */
    fun get(node: Annotation): String {
        return if (node.typeName.isQualifiedName) {
            node.typeName.fullyQualifiedName
        } else {
            if (!checkIfJmhAnnotation(node, node.typeName.fullyQualifiedName)) {
                node.typeName.fullyQualifiedName
            } else {
                JMHConstants.Package.annotation + "." + node.typeName.fullyQualifiedName
            }
        }
    }

    fun getClassName(node: TypeDeclaration): String {
        var fullyQualifiedName = ""
        var iterator: ASTNode? = node

        while (iterator != null) {
            when (iterator) {
                is TypeDeclaration -> {
                    fullyQualifiedName = if (fullyQualifiedName.isBlank()) {
                        iterator.name.identifier
                    } else {
                        "${iterator.name.identifier}$$fullyQualifiedName"
                    }
                }
                is CompilationUnit -> {
                    if (iterator.`package` == null) {
                        // its in the default package
                        fullyQualifiedName = node.name.identifier
                    } else {
                        fullyQualifiedName = iterator.`package`.name.fullyQualifiedName + ".$fullyQualifiedName"
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
                        val exact = it.name.fullyQualifiedName.contains("${JMHConstants.Package.annotation}.$typeName") && !it.isOnDemand
                        val star = it.name.fullyQualifiedName.contains(JMHConstants.Package.annotation) && it.isOnDemand

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

    fun checkIfInfrastructureClass(type: ASTNode, className: String, fqn: String): Boolean {
        if (type.toString().endsWith(className)) {
            var iterator: ASTNode? = type

            while (iterator != null) {
                if (iterator is CompilationUnit) {
                    iterator.imports().forEach {
                        if (it is ImportDeclaration) {
                            val exact = it.name.fullyQualifiedName.contains(fqn) && !it.isOnDemand
                            val star = it.name.fullyQualifiedName.contains(JMHConstants.Package.infra) && it.isOnDemand

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