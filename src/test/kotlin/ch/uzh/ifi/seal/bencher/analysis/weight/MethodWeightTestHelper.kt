package ch.uzh.ifi.seal.bencher.analysis.weight

import ch.uzh.ifi.seal.bencher.analysis.JarTestHelper

object MethodWeightTestHelper {

    val coreAmWeight = Pair(JarTestHelper.CoreA.m, 1.0)
    val coreBmWeight = Pair(JarTestHelper.CoreB.m, 2.0)
    val coreCmWeight = Pair(JarTestHelper.CoreC.m, 3.0)
    val coreDmWeight = Pair(JarTestHelper.CoreD.m, 4.0)

    fun csvPrios(del: Char): String =
            """
            org.sample.core.CoreA${del}m${del}1
            org.sample.core.CoreB${del}m${del}2
            org.sample.core.CoreC${del}m${del}3
            org.sample.core.CoreD${del}m${del}4
            """.trimIndent()

    fun csvPriosWithHeader(del: Char, firstHeader: String, vararg restHeader: String): String =
            restHeader.fold(firstHeader) { acc, he -> "$acc$del$he" } + "\n${csvPrios(del)}"

}
