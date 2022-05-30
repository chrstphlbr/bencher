package ch.uzh.ifi.seal.bencher.analysis.coverage

object CoveragePrinterReaderConstants {
    const val covStart = "Method:"
    const val benchStart = "Benchmark"
    const val methodStart = "Method"
    const val lineStart = "Line"
    const val paramDelimiter = ", "
    const val paramDelimiterChar = ','
    const val paramBracesOpen = '('
    const val paramBracesClosed = ')'
    const val paramAssignOp = '='
    const val paramListStart = '['
    const val paramListEnd = ']'
    const val paramClazz = "clazz"
    const val paramMethod = "name"
    const val paramParams = "params"
    const val paramJmhParams = "jmhParams"
    const val paramFile = "file"
    const val paramNumber = "number"
    const val paramMissedInstructions = "mi"
    const val paramMissedBranches = "mb"
    const val paramCoveredInstructions = "ci"
    const val paramCoveredBranches = "cb"
    const val emptyList = "[]"
    const val coverageLineDelimiter = ";"
    const val valueNull = "null"
}
