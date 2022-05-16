package ch.uzh.ifi.seal.bencher.analysis.change

import ch.uzh.ifi.seal.bencher.Line

interface LineChangeAssessment {
    fun lineChanged(l: Line, c: Change): Boolean

    fun lineChanged(l: Line, cs: Iterable<Change>): Boolean =
        cs.any { lineChanged(l, it) }
}

object LineChangeAssessmentImpl : LineChangeAssessment {
    override fun lineChanged(l: Line, c: Change): Boolean =
        when (c) {
            is LineChange -> hasLineChanged(l, c)
            is AdditionChange -> lineChanged(l, c.type)
            is DeletionChange -> lineChanged(l, c.type)
            // all other changes are not considered by LineChangeAssessmentImpl
            else -> false
        }

    private fun hasLineChanged(l: Line, c: LineChange): Boolean = l.file == c.line.file && l.number == c.line.number
}
