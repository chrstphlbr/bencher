package ch.uzh.ifi.seal.bencher.analysis.change

import ch.uzh.ifi.seal.bencher.Class
import ch.uzh.ifi.seal.bencher.Line
import ch.uzh.ifi.seal.bencher.Method

sealed class Change

data class MethodChange(
        val method: Method
) : Change()

data class ClassHeaderChange(
        val clazz: Class
) : Change()

data class ClassFieldChange(
        val clazz: Class,
        val field: String
) : Change()

data class ClassMethodChange(
        val clazz: Class,
        val method: Method
) : Change()

data class DeletionChange(
        val type: Change
) : Change()

data class AdditionChange(
        val type: Change
) : Change()

data class LineChange(
        val line: Line
) : Change()
