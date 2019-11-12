package ch.uzh.ifi.seal.bencher.analysis.finder.jdt

import org.eclipse.jdt.core.dom.Expression

object ExpressionHelper {
    fun convertToAny(expression: Expression): Any {
        val expressionValue = expression.resolveConstantExpressionValue()

        return expressionValue ?: expression.toString()
    }
}