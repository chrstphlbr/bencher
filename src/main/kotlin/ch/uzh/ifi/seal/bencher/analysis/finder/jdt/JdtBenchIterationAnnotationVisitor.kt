package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import ch.uzh.ifi.seal.bencher.analysis.finder.shared.BenchIterationAnnotation
import org.eclipse.jdt.core.dom.MemberValuePair
import org.eclipse.jdt.core.dom.NormalAnnotation
import org.eclipse.jdt.core.dom.QualifiedName
import org.eclipse.jdt.core.dom.SingleMemberAnnotation
import org.funktionale.option.Option
import java.util.concurrent.TimeUnit

class JdtBenchIterationAnnotationVisitor : ASTVisitorExtended() {

    val benchIterationAnnotation = BenchIterationAnnotation()

    // TODO remove
    fun iterations(): Int = benchIterationAnnotation.iterations()

    fun time(): Int = benchIterationAnnotation.time()
    fun timeUnit(): Option<TimeUnit> = benchIterationAnnotation.timeUnit()

    override fun visit(node: NormalAnnotation): Boolean {
        node.values().forEach {
            if (it is MemberValuePair) {
                val binding = it.value.resolveTypeBinding()
                if (binding.isEnum) {
                    benchIterationAnnotation.setValueEnum(it.name.identifier, BenchIterationAnnotation.bcTimeUnit, (it.value as QualifiedName).name.identifier)
                } else {
                    benchIterationAnnotation.setValue(it.name.identifier, it.value.resolveConstantExpressionValue())
                }
            }
        }
        return super.visit(node)
    }

    override fun visit(node: SingleMemberAnnotation): Boolean {
        benchIterationAnnotation.setValue(null, node.value.resolveConstantExpressionValue())
        return super.visit(node)
    }
}
