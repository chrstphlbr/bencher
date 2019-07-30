package ch.uzh.ifi.seal.bencher.analysis.finder.asm

import ch.uzh.ifi.seal.bencher.analysis.finder.jdt.ASTVisitorExtended
import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchOutputTimeUnitAnnotation
import org.eclipse.jdt.core.dom.MemberValuePair
import org.eclipse.jdt.core.dom.NormalAnnotation
import org.eclipse.jdt.core.dom.QualifiedName
import org.eclipse.jdt.core.dom.SingleMemberAnnotation
import org.funktionale.option.Option
import java.util.concurrent.TimeUnit

class JdtBenchOutputTimeUnitAnnotationVisitor : ASTVisitorExtended() {
    val benchOutputTimeUnitAnnotation = BenchOutputTimeUnitAnnotation()

    // TODO remove
    fun timeUnit(): Option<TimeUnit> = benchOutputTimeUnitAnnotation.timeUnit()

    override fun visit(node: NormalAnnotation): Boolean {
        node.values().forEach {
            if (it is MemberValuePair) {
                val binding = it.value.resolveTypeBinding()
                if (binding.isEnum) {
                    benchOutputTimeUnitAnnotation.setValueEnum(it.name.identifier, BenchOutputTimeUnitAnnotation.bcTimeUnit, (it.value as QualifiedName).name.identifier)
                }
            }
        }
        return super.visit(node)
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        if (node.value is QualifiedName) {
            benchOutputTimeUnitAnnotation.setValueEnum(null, BenchOutputTimeUnitAnnotation.bcTimeUnit, (node.value as QualifiedName).name.identifier)
        }

        return super.visit(node)
    }
}