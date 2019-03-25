package ch.uzh.ifi.seal.bencher.analysis.callgraph

object CGPrinterReaderConstants {
    val cgStart = "Method:"
    val benchStart = "Benchmark"
    val methodStart = "Method"
    val paramDelimiter = ", "
    val paramDelimiterChar = ','
    val paramBracesOpen = '('
    val paramBracesClosed = ')'
    val paramAssignOp = '='
    val paramListStart = '['
    val paramListEnd = ']'
    val paramClazz = "clazz"
    val paramMethod = "name"
    val paramParams = "params"
    val paramJmhParams = "jmhParams"
    val emptyList = "[]"
    val edgeLineDelimiter = ";"
}
